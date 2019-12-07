package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.temple.audiobookplayer.AudiobookService;
import edu.temple.bookcase.fragments.BookDetailsFragment;
import edu.temple.bookcase.fragments.BookListFragment;
import edu.temple.bookcase.fragments.HorizontalDoubleFragment;
import edu.temple.bookcase.fragments.PagerFragment;

public class MainActivity extends AppCompatActivity {

    int orientation;

    String searchParam = null;

    Book selected = null;

    List<Book> books = new ArrayList<>();
    ViewGroup holder;
    Fragment bookList;
    BookDetailsFragment bookDetails;
    SeekBar seekBar;

    AudiobookService.MediaControlBinder media = null;

    public ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            media = (AudiobookService.MediaControlBinder) binder;
        }

        public void onServiceDisconnected(ComponentName className) {
            media = null;
        }
    };

    boolean isTablet = false;

    public class myHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            AudiobookService.BookProgress obj = (AudiobookService.BookProgress) msg.obj;
            if (obj != null) {
                MainActivity.this.seekBar.setProgress(obj.getProgress());
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.checkPermissions();

        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.holder = this.findViewById(R.id.holder);
        this.bookDetails = BookDetailsFragment.newInstance();

        this.isTablet = this.getResources().getBoolean(R.bool.isTablet);
        this.orientation = this.getResources().getConfiguration().orientation;

        this.seekBar = this.findViewById(R.id.seekBar);

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (MainActivity.this.playingBook != null) {
                    media.seekTo(seekBar.getProgress());
                }
            }
        });

        this.loadBooks(null, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setForOrientation(int orientation) {
        this.holder.removeAllViews();
        Fragment frag;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            frag = HorizontalDoubleFragment.newInstance(this.bookList, this.bookDetails);
        } else {
            List<Fragment> list = new ArrayList<>();
            list.add(this.bookList);
            list.add(this.bookDetails);
            frag = PagerFragment.newInstance(list);
        }
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.holder, frag);
        ft.commit();
    }





    // DO STUFF WITH BOOKS

    public void selectBook(int index) {
        this.selected = this.books.get(index);
        this.bookDetails.displayBook(this.selected);
        this.media.setProgressHandler(new myHandler());
    }

    public void search(View view) {
        this.searchParam = ((EditText)this.findViewById(R.id.editText)).getText().toString();

        @SuppressLint("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {
            private String text = null;

            @Override
            protected void onPreExecute() {
                this.text = MainActivity.this.searchParam;
                Toast.makeText(MainActivity.this.getApplicationContext(), this.text, Toast.LENGTH_SHORT);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    File file = new File(MainActivity.this.getApplicationContext().getFilesDir(), "last.txt");
                    Objects.requireNonNull(file.getParentFile()).mkdirs();
                    file.createNewFile();
                    OutputStream output = new FileOutputStream(file);
                    output.write(text.getBytes());
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        task.execute();

        this.loadBooks(this.searchParam, false);
    }

    private Book playingBook = null;

    public void play(View view) {
        if (this.selected != null && this.bookDetails.book() != null) {
            @SuppressLint("StaticFieldLeak")
            AsyncTask task = new AsyncTask() {

                Context context = null;
                Book playBook = null;
                Book saveBook = null;
                int playProgress = 0;
                int saveProgress = 0;
                AudiobookService.MediaControlBinder media = null;

                Boolean local = false;

                @Override
                protected void onPreExecute() {
                    this.context = MainActivity.this.getApplicationContext();
                    this.playBook = MainActivity.this.bookDetails.book();
                    this.saveBook = MainActivity.this.playingBook;
                    this.saveProgress = ((SeekBar) MainActivity.this.findViewById(R.id.seekBar)).getProgress();
                    this.media = MainActivity.this.media;
                }

                @Override
                protected Object doInBackground(Object[] objects) {
                    if (this.context != null) {
                        if (this.saveBook != null) {
                            FileIO.saveBookMark(this.context.getFilesDir(), this.saveBook, this.saveProgress);
                        }

                        this.playProgress = FileIO.getBookMark(this.context.getFilesDir(), this.playBook);

                        if (FileIO.hasLocalAudio(this.context.getFilesDir(), this.playBook)) {
                            this.media.play(FileIO.getLocalAudio(this.context.getFilesDir(), this.playBook), this.playProgress);
                            this.local = true;
                        } else {
                            this.media.play(this.playBook.id(), this.playProgress);
                            this.local = false;
                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    MainActivity a = MainActivity.this;

                    ((TextView) a.findViewById(R.id.nowPlayingTitle)).setText(this.playBook.title());
                    ((TextView) a.findViewById(R.id.nowPlayingPaused)).setText("(Playing)");

                    a.playingBook = this.playBook;
                    a.seekBar.setProgress(this.playProgress);
                    a.seekBar.setMax(this.playBook.duration());
                    if (this.local) {
                        Toast.makeText(a.getApplicationContext(), "Playing from local storage", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(a.getApplicationContext(), "Playing from stream", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            task.execute();
        }
    }

    public void stop(View view) {
        if (this.playingBook != null) {
            @SuppressLint("StaticFieldLeak")
            AsyncTask task = new AsyncTask() {
                Context context = null;
                AudiobookService.MediaControlBinder media = null;
                Book stopBook = null;
                int stopProgress = 0;

                @Override
                protected void onPreExecute() {
                    MainActivity a = MainActivity.this;
                    this.context = a.getApplicationContext();
                    this.media = a.media;
                    this.stopBook = a.playingBook;
                    this.stopProgress = a.seekBar.getProgress();
                }

                @Override
                protected Object doInBackground(Object[] objects) {
                    FileIO.saveBookMark(this.context.getFilesDir(), this.stopBook, this.stopProgress);
                    this.media.stop();
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    MainActivity a = MainActivity.this;

                    ((TextView) a.findViewById(R.id.nowPlayingTitle)).setText("(None)");
                    ((TextView) a.findViewById(R.id.nowPlayingPaused)).setText("(Stopped)");
                    a.playingBook = null;
                    a.seekBar.setProgress(0);
                    a.seekBar.setMax(100);
                }
            };
            task.execute();
        }
    }

    public void togglePlay(View view) {
        if (this.playingBook != null) {
            @SuppressLint("StaticFieldLeak")
            AsyncTask task = new AsyncTask() {

                Context context = null;
                AudiobookService.MediaControlBinder media = null;
                Book toggleBook = null;
                Integer progress = null;

                Boolean paused = null;
                Boolean local = null;

                @Override
                protected void onPreExecute() {
                    MainActivity a = MainActivity.this;
                    this.context = a.getApplicationContext();
                    this.media = a.media;
                    this.toggleBook = a.playingBook;
                    this.progress = a.seekBar.getProgress();
                }

                @Override
                protected Object doInBackground(Object[] objects) {
                    if (this.media.isPlaying()) {
                        this.media.pause();
                        this.paused = true;
                    } else {
                        if (FileIO.hasLocalAudio(this.context.getFilesDir(), this.toggleBook)) {
                            this.media.play(FileIO.getLocalAudio(this.context.getFilesDir(), this.toggleBook), this.progress);
                            this.local = true;
                        } else {
                            this.media.play(this.toggleBook.id(), this.progress);
                            this.local = false;
                        }
                        this.paused = false;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    MainActivity a = MainActivity.this;

                    if (this.paused) {
                        ((TextView) a.findViewById(R.id.nowPlayingPaused)).setText("(Paused)");
                    } else {
                        ((TextView) a.findViewById(R.id.nowPlayingPaused)).setText("(Playing)");
                        if (this.local) {
                            Toast.makeText(a.getApplicationContext(), "Playing from local storage", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(a.getApplicationContext(), "Playing from stream", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };
            task.execute();
        }
    }

    public void downloadButton(View view) {
        if (this.bookDetails.book() != null) {
            @SuppressLint("StaticFieldLeak")
            AsyncTask task = new AsyncTask() {

                Context context = null;
                Book book = null;
                Boolean delete = null;

                @Override
                protected void onPreExecute() {
                    MainActivity a = MainActivity.this;
                    this.context = a.getApplicationContext();
                    this.book = a.bookDetails.book();

                    a.findViewById(R.id.download).setEnabled(false);
                }

                @Override
                protected Object doInBackground(Object[] objects) {
                    this.delete = FileIO.hasLocalAudio(this.context.getFilesDir(), this.book);

                    if (this.delete) {
                        FileIO.deleteLocalAudio(this.context.getFilesDir(), this.book);
                    } else {
                        FileIO.downloadLocalAudio(this.context.getFilesDir(), this.book);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    MainActivity a = MainActivity.this;
                    if (this.delete) {
                        ((TextView)a.findViewById(R.id.download)).setText("Download");
                    } else {
                        ((TextView)a.findViewById(R.id.download)).setText("Delete");
                    }
                    a.findViewById(R.id.download).setEnabled(true);
                }
            };
            task.execute();
        }
    }





    // BOOK GETTER

    public void loadBooks(String param, boolean loadOldSearch) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {
            Context context = null;
            private List<Book> books = null;

            @Override
            protected void onPreExecute() {
                MainActivity a = MainActivity.this;
                this.context = a.getApplicationContext();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                String param = (String)objects[0];
                Boolean loadOldSearch = (Boolean)objects[1];
                this.books = WebIO.downloadBooks(param, loadOldSearch, this.context.getFilesDir());
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                MainActivity a = MainActivity.this;
                a.books = this.books;
                a.bookList = BookListFragment.newInstance(a.books);
                a.bookDetails = BookDetailsFragment.newInstance();
                if (a.isTablet) {
                    a.setForOrientation(Configuration.ORIENTATION_LANDSCAPE);
                } else {
                    a.setForOrientation(MainActivity.this.orientation);
                }
            }
        };
        task.execute(param, loadOldSearch);
    }





    // PERMISSION STUFF

    private static int REQUEST_WRITE = 1;
    private static int REQUEST_READ = 2;

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ);
        }
    }
}
