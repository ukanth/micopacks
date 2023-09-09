package dev.ukanth.iconmgr.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface FavDao {


    @Insert
    void insertFavorite(Favorite favorite);

    @Update
    void updateFavorite(Favorite favorite);

    @Query("SELECT iconImageData FROM Favorite WHERE fav = 1 ")
    List<byte[]> getIconImageData();

    @Query("SELECT fav FROM Favorite WHERE iconPkg = :packageName AND Icontitle = :iconTitle AND iconName = :iconName")
    boolean isFavorite(String packageName, String iconTitle, String iconName);
}

