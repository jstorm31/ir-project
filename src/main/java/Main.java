import java.io.IOException;

public class Main {

    static String indexDirPath = "/Users/jstorm31/Downloads/stackoverflow/index";
    static String docDirPath = "/Users/jstorm31/Downloads/stackoverflow";

    public static void main(String[] args) {
        try {
            IndexBuilder builder = new IndexBuilder(indexDirPath);
            builder.build(docDirPath);
            System.out.println("Index has been successfully built");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}