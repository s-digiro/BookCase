package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.JsonReader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import edu.temple.audiobookplayer.AudiobookService;
import edu.temple.bookcase.fragments.BookDetailsFragment;
import edu.temple.bookcase.fragments.BookListFragment;
import edu.temple.bookcase.fragments.HorizontalDoubleFragment;
import edu.temple.bookcase.fragments.PagerFragment;

public class MainActivity extends AppCompatActivity {

    int orientation;

    Book selected = null;

    List<Book> books = new ArrayList<>();
    ViewGroup holder;
    Fragment bookList;
    BookDetailsFragment bookDetails;

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
            if (msg != null) {
                super.handleMessage(msg);
                AudiobookService.BookProgress obj = (AudiobookService.BookProgress) msg.obj;
                if (obj != null) {
                    MainActivity.this.progress = obj.getProgress();
                    if (MainActivity.this.playingBook != null) {
                        String s = MainActivity.this.progress + "/" + MainActivity.this.playingBook.duration();
                        ((TextView) MainActivity.this.findViewById(R.id.nowPlayingProgress)).setText(s);
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.holder = this.findViewById(R.id.holder);
        this.bookDetails = BookDetailsFragment.newInstance();

        this.isTablet = this.getResources().getBoolean(R.bool.isTablet);
        this.orientation = this.getResources().getConfiguration().orientation;

        this.doAsync("https://kamorris.com/lab/audlib/booksearch.php");
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

    public void selectBook(int index) {
        this.selected = this.books.get(index);
        this.bookDetails.displayBook(this.selected);
        this.media.setProgressHandler(new myHandler());
    }

    public void search(View view) {
        String param = ((EditText)this.findViewById(R.id.editText)).getText().toString();
        String path = "https://kamorris.com/lab/audlib/booksearch.php?search=";
        path += param;

        this.doAsync(path);
    }

    private boolean playing = false;
    private Book playingBook = null;
    private Integer progress = null;

    public void play(View view) {
        if (this.selected != null) {
            ((TextView) this.findViewById(R.id.nowPlayingTitle)).setText(this.selected.title());
            ((TextView) this.findViewById(R.id.nowPlayingPaused)).setText("(Playing)");
            this.media.play(this.selected.id());
            this.playing = true;
            this.playingBook = this.selected;
        }
    }

    public void stop(View view) {
        ((TextView) this.findViewById(R.id.nowPlayingTitle)).setText("(None)");
        ((TextView) this.findViewById(R.id.nowPlayingPaused)).setText("(Stopped)");
        this.media.stop();
        this.playing = false;
        this.playingBook = null;
    }

    public void togglePlay(View view) {
        if (this.playingBook != null) {
            if (this.playing) {
                ((TextView) this.findViewById(R.id.nowPlayingPaused)).setText("(Paused)");
                this.media.pause();
            } else {
                ((TextView) this.findViewById(R.id.nowPlayingPaused)).setText("(Playing)");
                this.media.play(this.playingBook.id(), this.progress);
            }
            this.playing = !this.playing;
        }
    }

    public void seekForward(View view) {
        if (this.playingBook != null) {
            int seekTo = this.progress + 10;
            if (seekTo >= this.playingBook.duration()) {
                seekTo = this.playingBook.duration() - 1;
            }
            media.seekTo(seekTo);
        }
    }

    public void seekBack(View view) {
        if (this.playingBook != null) {
            int seekTo = this.progress - 10;
            if (seekTo <= 0) {
                seekTo = 1;
            }
            media.seekTo(seekTo);
        }
    }

    public void doAsync(String path) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {

            private List<Book> books = new ArrayList<>();

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    this.books = new ArrayList<>();
                    String path = (String)objects[0];
                    URL api = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection)api.openConnection();
                    if (conn.getResponseCode() == 200) {
                        InputStream responseBody = conn.getInputStream();
                        InputStreamReader reader = new InputStreamReader(responseBody, StandardCharsets.UTF_8);
                        JsonReader jsonReader = new JsonReader(reader);
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            Book.Builder builder = Book.Builder.newInstance();
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()) {
                                String key = jsonReader.nextName();
                                switch (key) {
                                    case "book_id":
                                        builder.setId(Integer.parseInt(jsonReader.nextString()));
                                        break;
                                    case "title":
                                        builder.setTitle(jsonReader.nextString());
                                        break;
                                    case "author":
                                        builder.setAuthor(jsonReader.nextString());
                                        break;
                                    case "published":
                                        builder.setPublished(Integer.parseInt(jsonReader.nextString()));
                                        break;
                                    case "cover_url":
                                        builder.setCoverURL(jsonReader.nextString());
                                        break;
                                    case "duration":
                                        builder.setDuration(Integer.parseInt(jsonReader.nextString()));
                                        break;
                                    default:
                                        jsonReader.nextString();
                                        break;
                                }
                            }
                            jsonReader.endObject();
                            this.books.add(builder.build());
                        }
                        jsonReader.endArray();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                MainActivity.this.books = this.books;
                MainActivity.this.bookList = BookListFragment.newInstance(MainActivity.this.books);
                MainActivity.this.bookDetails = BookDetailsFragment.newInstance();
                if (MainActivity.this.isTablet) {
                    MainActivity.this.setForOrientation(Configuration.ORIENTATION_LANDSCAPE);
                } else {
                    MainActivity.this.setForOrientation(MainActivity.this.orientation);
                }
            }
        };
        task.execute(path);
    }
}
