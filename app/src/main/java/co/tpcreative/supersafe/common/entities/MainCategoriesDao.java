package co.tpcreative.supersafe.common.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MainCategoriesDao {
    @Insert
    void insert(MainCategoryEntity... items);

    @Update
    void update(MainCategoryEntity... items);

    @Delete
    void delete(MainCategoryEntity... items);

    @Query("DELETE FROM maincategories")
    public void deleteAllCategories();


    @Query("Delete from maincategories  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    void deleteAll(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM maincategories WHERE id = :id AND isFakePin =:isFakePin")
    MainCategoryEntity loadItemId(int id, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name AND isFakePin =:isFakePin")
    MainCategoryEntity loadItemId(String categories_hex_name, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name AND isFakePin =:isFakePin")
    List<MainCategoryEntity> loadListAllItemId(String categories_hex_name, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    MainCategoryEntity loadItemLocalId(String categories_local_id, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_id = :categories_id AND isFakePin =:isFakePin")
    MainCategoryEntity loadItemCategoriesId(String categories_id, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin")
    MainCategoryEntity loadItemCategoriesSync(boolean isSyncOwnServer, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin")
    List<MainCategoryEntity> loadListItemCategoriesSync(boolean isSyncOwnServer, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin ORDER BY id DESC LIMIT :limit")
    List<MainCategoryEntity> loadListItemCategoriesSync(boolean isSyncOwnServer, int limit, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id AND isFakePin=:isFakePin")
    List<MainCategoryEntity> loadListItemId(String categories_local_id, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isDelete = :isDelete AND isFakePin =:isFakePin")
    List<MainCategoryEntity> loadAll(boolean isDelete, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isChange = :isChange AND isFakePin =:isFakePin")
    List<MainCategoryEntity> loadAllChangedItem(boolean isChange, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isDelete = :isDelete AND isFakePin =:isFakePin AND categories_local_id !=:categories_local_id")
    List<MainCategoryEntity> loadAll(String categories_local_id, boolean isDelete, boolean isFakePin);

    @Query("Select * FROM maincategories WHERE isFakePin =:isFakePin")
    List<MainCategoryEntity> loadAll(boolean isFakePin);

    @Query("Select * FROM maincategories  ORDER BY id DESC LIMIT :limit")
    MainCategoryEntity loadLatestItem(int limit);

    @Query("Select * FROM maincategories  WHERE categories_local_id = :categories_local_id")
    MainCategoryEntity loadLocalId(String  categories_local_id);

}
