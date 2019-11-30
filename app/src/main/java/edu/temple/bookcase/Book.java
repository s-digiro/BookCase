package edu.temple.bookcase;

public class Book {

    private int id;
    private String title;
    private String author;
    private int published;
    private String coverURL;
    private int duration;

    public Book(int id, String title, String author, int published, String coverURL, int duration) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.published = published;
        this.coverURL = coverURL;
        this.duration = duration;
    }

    public int id() {
        return this.id;
    }

    public String title() {
        return this.title;
    }

    public String author() {
        return this.author;
    }

    public int published() {
        return this.published;
    }

    public String cover() {
        return this.coverURL;
    }

    public int duration() {
        return this.duration;
    }

    public static class Builder {

        private int id;
        private String title;
        private String author;
        private int published;
        private String coverURL;
        private int duration;

        public static Builder newInstance() {
            return new Builder();
        }

        public Book build() {
            return new Book(this.id, this.title, this.author, this.published, this.coverURL, this.duration);
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setPublished(int published) {
            this.published = published;
            return this;
        }

        public Builder setCoverURL(String coverURL) {
            this.coverURL = coverURL;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }
    }
}
