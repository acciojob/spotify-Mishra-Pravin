package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Artist, List<Song>> artistSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;
    public HashMap<Artist, List<User>> artistLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository() {
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        artistSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();
        artistLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);

        return user;
    }

    private Optional<User> findUser(String mobile) {
        for (User user : users)
            if (user.getMobile().equals(mobile))
                return Optional.of(user);

        return Optional.empty();
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);

        return artist;
    }

    private Optional<Artist> findArtist(String artistName) {
        for (Artist artist : artists)
            if (artist.getName().equals(artistName))
                return Optional.of(artist);

        return Optional.empty();
    }

    public Album createAlbum(String title, String artistName) {
        Optional<Artist> optionalArtist = findArtist(artistName);
        if (optionalArtist.isEmpty()) {
            optionalArtist = Optional.of(createArtist(artistName));
            artists.add(optionalArtist.get());
        }
        Artist artist = optionalArtist.get();

        Album album = new Album(title);
        albums.add(album);

        List<Album> albumList = artistAlbumMap.getOrDefault(artist, new ArrayList<>());
        albumList.add(album);

        artistAlbumMap.put(artist, albumList);

        return album;
    }

    public Optional<Album> findAlbum(String albumName) {
        for (Album album : albums)
            if (album.getTitle().equals(albumName))
                return Optional.of(album);

        return Optional.empty();
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Optional<Album> optionalAlbum = findAlbum(albumName);
        if (optionalAlbum.isEmpty())
            throw new Exception("Album does not exist");
        Album album = optionalAlbum.get();

        Song song = new Song(title, length);
        songs.add(song);

        List<Song> songList = albumSongMap.getOrDefault(album, new ArrayList<>());
        songList.add(song);
        albumSongMap.put(album, songList);

        for (Artist artist : artistAlbumMap.keySet())
            if (artistAlbumMap.get(artist).contains(album)) {
                songList = artistSongMap.getOrDefault(artist, new ArrayList<>());
                songList.add(song);
                artistSongMap.put(artist, songList);
                break;
            }

        return song;
    }

    private Optional<Song> findSong(String songTitle) {
        for (Song song : songs)
            if (song.getTitle().equals(songTitle))
                return Optional.of(song);

        return Optional.empty();
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Optional<User> optionalUser = findUser(mobile);
        if (optionalUser.isEmpty())
            throw new Exception("User does not exist");
        User user = optionalUser.get();

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = playlistSongMap.getOrDefault(playlist, new ArrayList<>());
        for (Song song : songs)
            if (song.getLength() == length)
                songList.add(song);

        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user, playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user);
        playlistListenerMap.put(playlist, listenerList);

        List<Playlist> playlistList = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user, playlistList);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Optional<User> optionalUser = findUser(mobile);
        if (optionalUser.isEmpty())
            throw new Exception("User does not exist");
        User user = optionalUser.get();

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> songList = playlistSongMap.getOrDefault(playlist, new ArrayList<>());
        for (Song song : songs)
            for (String songTitle : songTitles)
                if (song.getTitle().equals(songTitle))
                    songList.add(song);

        playlistSongMap.put(playlist, songList);

        creatorPlaylistMap.put(user, playlist);

        List<User> listenerList = new ArrayList<>();
        listenerList.add(user);
        playlistListenerMap.put(playlist, listenerList);

        List<Playlist> playlistList = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlistList.add(playlist);
        userPlaylistMap.put(user, playlistList);

        return playlist;
    }

    private Optional<Playlist> findPlaylistByTitle(String playlistTitle) {
        for (Playlist playlist : playlists)
            if (playlist.getTitle().equals(playlistTitle))
                return Optional.of(playlist);

        return Optional.empty();
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        if (!StringUtils.hasText(mobile))
            throw new Exception("Invalid mobile number");

        if (!StringUtils.hasText(playlistTitle))
            throw new Exception("Invalid playlist title");

        Optional<User> optionalUser = findUser(mobile);
        if (optionalUser.isEmpty())
            throw new Exception("User does not exist");
        User user = optionalUser.get();

        Optional<Playlist> optionalPlaylist = findPlaylistByTitle(playlistTitle);
        if (optionalPlaylist.isEmpty())
            throw new Exception("Playlist does not exist");
        Playlist playlist = optionalPlaylist.get();

        if (!creatorPlaylistMap.containsKey(user) || creatorPlaylistMap.get(user) != playlist) {
            List<User> listenerList = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());

            if (!listenerList.contains(user)) {
                listenerList.add(user);
                playlistListenerMap.put(playlist, listenerList);
            }
        }

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        Optional<User> optionalUser = findUser(mobile);
        if (optionalUser.isEmpty())
            throw new Exception("User does not exist");
        User user = optionalUser.get();

        Optional<Song> optionalSong = findSong(songTitle);
        if (optionalSong.isEmpty())
            throw new Exception("Song does not exist");
        Song song = optionalSong.get();

        if (!songLikeMap.containsKey(song) || !songLikeMap.get(song).contains(user)) {
            List<User> userList = songLikeMap.getOrDefault(song, new ArrayList<>());
            userList.add(user);
            songLikeMap.put(song, userList);

            Artist artist = null;
            for (Artist a : artistSongMap.keySet())
                if (artistSongMap.get(a).contains(song))
                    artist = a;

            userList = artistLikeMap.getOrDefault(artist, new ArrayList<>());
            userList.add(user);
            artistLikeMap.put(artist, userList);
        }

        return song;
    }

    public String mostPopularArtist() {
        int likes = 0;
        String artistName = "";

        for (Artist artist : artistLikeMap.keySet()) {
            int size = artistLikeMap.get(artist).size();
            if (size > likes) {
                likes = size;
                artistName = artist.getName();
            }
        }

        return artistName;
    }

    public String mostPopularSong() {
        int likes = 0;
        String songName = "";

        for (Song song : songLikeMap.keySet()) {
            int size = songLikeMap.get(song).size();
            if (size > likes) {
                likes = size;
                songName = song.getTitle();
            }
        }

        return songName;
    }
}