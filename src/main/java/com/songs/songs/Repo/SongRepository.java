package com.songs.songs.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.songs.songs.Entity.Song;
import com.songs.songs.Entity.User;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByUser(User user);

}
