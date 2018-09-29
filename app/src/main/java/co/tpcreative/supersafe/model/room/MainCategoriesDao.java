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

    @Query("Delete from maincategories  WHERE categories_local_id = :categories_local_id")
    void deleteAll(String categories_local_id);

    @Query("Select * FROM maincategories WHERE id = :id")
    MainCategories loadItemId(int id);

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name")
    MainCategories loadItemId(String categories_hex_name);

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name")
    List<MainCategories> loadListAllItemId(String categories_hex_name);

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id")
    MainCategories loadItemLocalId(String categories_local_id);

    @Query("Select * FROM maincategories WHERE categories_id = :categories_id")
    MainCategories loadItemCategoriesId(String categories_id);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer")
    MainCategories loadItemCategoriesSync(boolean isSyncOwnServer);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer")
    List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer ORDER BY id DESC LIMIT :limit")
    List<MainCategories> loadListItemCategoriesSync(boolean isSyncOwnServer,int limit);

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id")
    List<MainCategories> loadListItemId(String categories_local_id);

    @Query("Select * FROM maincategories")
    List<MainCategories> loadAll();

}
