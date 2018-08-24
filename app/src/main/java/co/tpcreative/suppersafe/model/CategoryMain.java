package co.tpcreative.suppersafe.model;

public class CategoryMain {

    private final String name;
    private final int imageResource;
    private final int id;

    public CategoryMain(int id,String name,int imageResource ) {
        this.name = name;
        this.imageResource = imageResource;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getId(){
        return id;
    }


}
