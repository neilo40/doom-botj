import java.io.IOException;

public class WadParserRunner {
    public static void main(String[] args) {
        WadParser wadParser = new WadParser();
        Wad wad = null;
        try {
            wad = wadParser.createWad("/home/neil/Downloads/doom1.wad");
        } catch (IOException e){
            System.out.println("Could not find file");
            System.exit(1);
        }
        System.out.println(wad.getWadType());
    }
}
