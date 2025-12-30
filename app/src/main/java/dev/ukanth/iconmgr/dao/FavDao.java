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

    @Query("DELETE FROM favorite WHERE iconImageData = :imageData")
    void deleteIcon(byte[] imageData);

    @Query("DELETE FROM Favorite WHERE iconPkg = :packageName AND Icontitle = :iconTitle AND iconName = :iconName")
    void deleteFavorite(String packageName, String iconTitle, String iconName);

    @Query("SELECT Icontitle FROM Favorite WHERE iconImageData = :iconImageData LIMIT 1")
    String getIcontitleForIconImageData(byte[] iconImageData);

    @Query("SELECT iconImageData FROM Favorite WHERE iconName = :iconName ")
    List<byte[]> getIconImageData(String iconName);


    @Query("SELECT EXISTS (SELECT 1  FROM Favorite WHERE iconPkg = :packageName AND Icontitle = :iconTitle AND iconName = :iconName)")
    boolean isFavorite(String packageName, String iconTitle, String iconName);

    @Query("SELECT DISTINCT iconName FROM Favorite")
    List<String> geticonName();
    
    @Query("SELECT * FROM Favorite")
    List<Favorite> getAllFavorites();
}

