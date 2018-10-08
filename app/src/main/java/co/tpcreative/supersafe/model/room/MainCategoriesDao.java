package co.tpcreative.supersafe.model.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;

@Dao
public interface MainCategoriesDao {
    @Insert
    void insert(MainCategories... items);

    @Update
    void update(MainCategories... items);

    @Delete
    void delete(MainCategories... items);

    @Query("Delete from maincategories  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    void deleteAll(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE id = :id AND isFakePin =:isFakePin")
    MainCategories loadItemId(int id,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name AND isFakePin =:isFakePin")
    MainCategories loadItemId(String categories_hex_name,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name AND isFakePin =:isFakePin")
    List<MainCategories> loadListAllItemId(String categories_hex_name,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    MainCategories loadItemLocalId(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_id = :categories_id AND isFakePin =:isFakePin")
    MainCategories loadItemCategoriesId(String categories_id,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin")
    MainCategories loadItemCategoriesSync(boolean isSyncOwnServer,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin")
    List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin ORDER BY id DESC LIMIT :limit")
    List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer,int limit,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id AND isFakePin=:isFakePin")
    List<MainCategories> loadListItemId(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isDelete = :isDelete AND isFakePin =:isFakePin")
    List<MainCategories> loadAll(boolean isDelete,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isFakePin =:isFakePin")
    List<MainCategories> loadAll(boolean isFakePin);

}
