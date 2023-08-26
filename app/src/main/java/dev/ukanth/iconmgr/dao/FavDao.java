package dev.ukanth.iconmgr.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface FavDao {


    @Insert
    void insertFavorite(Favorite favorite);

    @Update
    void updateFavorite(Favorite favorite);

    @Query("SELECT COUNT(*) FROM Favorite WHERE iconName = :iconName AND fav = 1")
    int isFavorite(String iconName);


}

