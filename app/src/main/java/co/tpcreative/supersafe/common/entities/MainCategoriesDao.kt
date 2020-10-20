package co.tpcreative.supersafe.common.entities
import androidx.room.*

@Dao
interface MainCategoriesDao {
    @Insert
    open fun insert(vararg items: MainCategoryEntity?)

    @Update
    open fun update(vararg items: MainCategoryEntity?)

    @Delete
    open fun delete(vararg items: MainCategoryEntity?)

    @Query("DELETE FROM maincategories")
    open fun deleteAllCategories()

    @Query("Delete from maincategories  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    open fun deleteAll(categories_local_id: String?, isFakePin: Boolean)

    @Query("Select * FROM maincategories WHERE id = :id AND isFakePin =:isFakePin")
    open fun loadItemId(id: Int, isFakePin: Boolean): MainCategoryEntity?

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name AND isFakePin =:isFakePin")
    open fun loadItemId(categories_hex_name: String?, isFakePin: Boolean): MainCategoryEntity?

    @Query("Select * FROM maincategories WHERE categories_hex_name = :categories_hex_name AND isFakePin =:isFakePin")
    open fun loadListAllItemId(categories_hex_name: String?, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    open fun loadItemLocalId(categories_local_id: String?, isFakePin: Boolean): MainCategoryEntity?

    @Query("Select * FROM maincategories WHERE categories_id = :categories_id AND isFakePin =:isFakePin")
    open fun loadItemCategoriesId(categories_id: String?, isFakePin: Boolean): MainCategoryEntity?

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin")
    open fun loadItemCategoriesSync(isSyncOwnServer: Boolean, isFakePin: Boolean): MainCategoryEntity?

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin")
    open fun loadListItemCategoriesSync(isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE isSyncOwnServer = :isSyncOwnServer AND isFakePin =:isFakePin ORDER BY id DESC LIMIT :limit")
    open fun loadListItemCategoriesSync(isSyncOwnServer: Boolean, limit: Int, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE categories_local_id = :categories_local_id AND isFakePin=:isFakePin")
    open fun loadListItemId(categories_local_id: String?, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE isDelete = :isDelete AND isFakePin =:isFakePin")
    open fun loadAll(isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE isChange = :isChange AND isFakePin =:isFakePin")
    open fun loadAllChangedItem(isChange: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE isDelete = :isDelete AND isFakePin =:isFakePin AND categories_local_id !=:categories_local_id")
    open fun loadAll(categories_local_id: String?, isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories WHERE isFakePin =:isFakePin")
    open fun loadAll(isFakePin: Boolean): MutableList<MainCategoryEntity>?

    @Query("Select * FROM maincategories  ORDER BY id DESC LIMIT :limit")
    open fun loadLatestItem(limit: Int): MainCategoryEntity?

    @Query("Select * FROM maincategories  WHERE categories_local_id = :categories_local_id")
    open fun loadLocalId(categories_local_id: String?): MainCategoryEntity?
}