import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

public class WadParser {
    private Integer HEADER_SIZE = 12;

    public Wad createWad(String fromFile) throws IOException {
        MappedByteBuffer byteStream = createStream(fromFile);
        String wadType = extractWadType(byteStream);
        Integer numLumps = extractNumLumps(byteStream); // Unused but still needs to be popped
        ByteBuffer data = extractData(byteStream);
        ArrayList<Lump> lumps = extractLumps(byteStream, data);
        Stream<Level> levels = extractLevels(lumps)
                .sorted(Comparator.comparing(Level::getName));
        return new Wad(wadType, levels.collect(Collectors.toCollection(ArrayList::new)));
    }

    private MappedByteBuffer createStream(String fromFile) throws IOException {
        File file = new File(fromFile);
        long fileSize = file.length();
        FileInputStream stream = new FileInputStream(file);
        MappedByteBuffer buffer = stream.getChannel().map(READ_ONLY, 0, fileSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    private String extractWadType(MappedByteBuffer byteStream) {
        byte[] wadTypeBytes = new byte[4];
        byteStream.get(wadTypeBytes, 0, 4);
        return new String(wadTypeBytes);
    }

    private Integer extractNumLumps(MappedByteBuffer byteStream) {
        return byteStream.getInt();
    }

    private ByteBuffer extractData(MappedByteBuffer byteStream) {
        Integer dataEnd = byteStream.getInt();
        ByteBuffer dataBytes = byteStream.slice();
        byteStream.position(dataEnd);
        return dataBytes;
    }

    private ArrayList<Lump> extractLumps(MappedByteBuffer byteStream, ByteBuffer data) {
        if (byteStream.remaining() == 0) {
            return new ArrayList<>();
        } else {
            Lump lump = extractLump(byteStream, data);
            ArrayList<Lump> lumps = extractLumps(byteStream, data);
            lumps.add(lump);
            return lumps;
        }
    }

    private Lump extractLump(MappedByteBuffer byteStream, ByteBuffer data) {
        Integer filePos = byteStream.getInt() - HEADER_SIZE;
        Integer size = byteStream.getInt();
        byte[] nameBytes = new byte[8];
        byteStream.get(nameBytes, 0, 8);
        String name = new String(nameBytes);
        byte[] dataBytes = new byte[size];
        if (filePos >= 0) {
            data.position(filePos);
            data.get(dataBytes, 0, size);
            return new Lump(name, Arrays.asList(ArrayUtils.toObject(dataBytes)));
        } else {
            return new Lump(name, new ArrayList<>());
        }
    }

    private Stream<Level> extractLevels(ArrayList<Lump> lumps) {

    }
}
