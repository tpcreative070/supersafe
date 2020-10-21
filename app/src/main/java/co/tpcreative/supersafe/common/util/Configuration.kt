package co.tpcreative.supersafe.common.util
class Configuration private constructor(builder: Configuration.Builder) {
    /**
     * Flag to indicate that this view has a camera button
     */
    var hasCamera: Boolean

    /**
     * Flag to indicate that this photo view has a layer mask
     */
    var hasShade: Boolean

    /**
     * Flag to indicate that this photo view clickable
     */
    var hasPreview: Boolean

    /**
     * GridView spacing between rows and columns
     */
    var spaceSize: Int

    /**
     * Maximum width of photos
     */
    var photoMaxWidth: Int

    /**
     * Checkbox background color
     */
    var checkBoxColor: Int

    /**
     * ablums dialog height
     */
    var dialogHeight: Int

    /**
     * ablums dialog title
     */
    var ablumsTitle: String?

    /**
     * maximum photos selection limit
     */
    var maximum: Int

    /**
     * ablums dialog mode
     *
     * DIALOG_FULL or DIALOG_HALF
     */
    var dialogMode: Int

    /**
     * Toast of maximum photos selection limit
     */
    var tip: String?
    var localCategoriesId: String?
    var isFakePIN: Boolean

    class Builder {
        var hasCamera: Boolean
        var hasShade: Boolean
        var hasPreview: Boolean
        var spaceSize: Int
        var photoMaxWidth: Int
        var checkBoxColor: Int
        var dialogHeight: Int
        var dialogMode: Int
        var ablumsTitle: String?
        var maximum: Int
        var tip: String?
        var localCategoriesId: String?
        var isFakePIN: Boolean

        constructor() {
            hasCamera = true
            hasShade = true
            hasPreview = true
            spaceSize = 4
            photoMaxWidth = 120
            checkBoxColor = -0xc0ae4b
            dialogHeight = Configuration.Companion.DIALOG_HALF
            dialogMode = Configuration.Companion.DIALOG_GRID
            maximum = 9
            tip = null
            ablumsTitle = null
            localCategoriesId = null
            isFakePIN = false
        }

        constructor(cfg: Configuration) {
            hasCamera = cfg.hasCamera
            hasShade = cfg.hasShade
            hasPreview = cfg.hasPreview
            spaceSize = cfg.spaceSize
            photoMaxWidth = cfg.photoMaxWidth
            checkBoxColor = cfg.checkBoxColor
            dialogHeight = cfg.dialogHeight
            dialogMode = cfg.dialogMode
            maximum = cfg.maximum
            tip = cfg.tip
            ablumsTitle = cfg.ablumsTitle
            localCategoriesId = cfg.localCategoriesId
            isFakePIN = cfg.isFakePIN
        }

        fun hasCamera(hasCamera: Boolean): Configuration.Builder? {
            this.hasCamera = hasCamera
            return this
        }

        fun hasShade(hasShade: Boolean): Configuration.Builder? {
            this.hasShade = hasShade
            return this
        }

        fun hasPreview(hasPreview: Boolean): Configuration.Builder? {
            this.hasPreview = hasPreview
            return this
        }

        fun setSpaceSize(spaceSize: Int): Configuration.Builder? {
            this.spaceSize = spaceSize
            return this
        }

        fun setPhotoMaxWidth(photoMaxWidth: Int): Configuration.Builder? {
            this.photoMaxWidth = photoMaxWidth
            return this
        }

        fun setCheckBoxColor(checkBoxColor: Int): Configuration.Builder? {
            this.checkBoxColor = checkBoxColor
            return this
        }

        fun setDialogHeight(dialogHeight: Int): Configuration.Builder? {
            this.dialogHeight = dialogHeight
            return this
        }

        fun setDialogMode(dialogMode: Int): Configuration.Builder? {
            this.dialogMode = dialogMode
            return this
        }

        fun setAblumsTitle(ablumsTitle: String?): Configuration.Builder? {
            this.ablumsTitle = ablumsTitle
            return this
        }

        fun setMaximum(maximum: Int): Configuration.Builder? {
            this.maximum = maximum
            return this
        }

        fun setTip(tip: String?): Configuration.Builder? {
            this.tip = tip
            return this
        }

        fun setLocalCategoriesId(localCategoriesId: String?): Configuration.Builder? {
            this.localCategoriesId = localCategoriesId
            return this
        }

        fun setFakePIN(fakePIN: Boolean): Configuration.Builder? {
            isFakePIN = fakePIN
            return this
        }

        fun build(): Configuration? {
            return Configuration(this)
        }
    }

    companion object {
        /**
         * ablums dialog full screen
         */
        const val DIALOG_FULL = -1

        /**
         * ablums dialog half screen
         */
        const val DIALOG_HALF = -2

        /**
         * ablums dialog mode grid view
         */
        const val DIALOG_GRID = -1

        /**
         * ablums dialog mode list view
         */
        const val DIALOG_LIST = -2
    }

    init {
        hasCamera = builder.hasCamera
        spaceSize = builder.spaceSize
        photoMaxWidth = builder.photoMaxWidth
        checkBoxColor = builder.checkBoxColor
        dialogHeight = builder.dialogHeight
        maximum = builder.maximum
        tip = builder.tip
        ablumsTitle = builder.ablumsTitle
        hasShade = builder.hasShade
        dialogMode = builder.dialogMode
        hasPreview = builder.hasPreview
        localCategoriesId = builder.localCategoriesId
        isFakePIN = builder.isFakePIN
    }
}