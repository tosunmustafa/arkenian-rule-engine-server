package com.arkenian.ruleengine.utility;

import com.arkenian.ruleengine.model.AbstractSubject;
import com.arkenian.ruleengine.model.Subject;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

@SuppressWarnings("unchecked")
public class IOUtility {

    public static Subject getSubject(Long oid, String homePath) throws IOException {
        String file = file(oid, homePath);
        return readFile(file, Subject.class, readHashFile(hashFile(oid, homePath)));
    }

    public static void createSubject(Subject subject, String homePath) throws IOException {
        reset(path(subject.getOid(), homePath));
        byte[] objectBytes = subject.toBuffer().toString().getBytes(); //SerializationUtils.serialize(subject);
        byte[] hashBytes = DigestUtils.md5DigestAsHex(objectBytes).getBytes();
        createFile(ByteBuffer.wrap(objectBytes), file(subject.getOid(), homePath));
        createFile(ByteBuffer.wrap(hashBytes), hashFile(subject.getOid(), homePath));
    }

    private static void reset(String folder) {
        if (Files.exists(Paths.get(folder))) {
            new File(folder).delete();
        }
        new File(folder).mkdir();
    }

    private static String readHashFile(String file) throws IOException {
        File f = new File(file);
        byte[] hashBytes = new byte[Long.valueOf(f.length()).intValue()];
        new FileInputStream(f).read(hashBytes);
        return new String(hashBytes);
    }

    private static <T extends AbstractSubject> T readFile(String file, Class<T> clazz, String hash) throws IOException {
        T object;
        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(
                Paths.get(file),
                EnumSet.of(StandardOpenOption.READ))
        ) {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            if (mappedByteBuffer != null) {
                object = mapObject(fileChannel, mappedByteBuffer, clazz, hash);
            } else {
                throw new StreamCorruptedException("Object memory mapping failed.");
            }
        }
        return object;
    }

    private static <T extends AbstractSubject> T mapObject(FileChannel channel, MappedByteBuffer buffer, Class<T> clazz, String hash) throws IOException {
        T object = null;
        byte[] serializedForm = new byte[buffer.remaining()];
        buffer.get(serializedForm);
        if (verify(hash, new ByteArrayInputStream(serializedForm))) {
            //object = clazz.cast(SerializationUtils.deserialize(serializedForm));
            try {
                Method method = clazz.getMethod("fromBuffer", String.class, Class.class);
                method.setAccessible(true);
                object = (T) method.invoke(null, new String(serializedForm), clazz);
            } catch (Exception e) {
                // TODO ??
            }
        } else {
            throw new StreamCorruptedException("Object hash verification failed.");
        }
        unMapBuffer(buffer, channel.getClass());
        return object;
    }

    private static boolean verify(String hash, ByteArrayInputStream is) throws IOException {
        boolean result = true;
        if (StringUtils.hasText(hash)) {
            result = DigestUtils.md5DigestAsHex(is).equals(hash);
        }
        return result;
    }

    private static void createFile(ByteBuffer buffer, String file) throws IOException {
        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(
                Paths.get(file),
                EnumSet.of(StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE))
        ) {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, buffer.remaining());
            if (mappedByteBuffer != null) {
                mappedByteBuffer.put(buffer);
                unMapBuffer(mappedByteBuffer, fileChannel.getClass());
            } else {
                throw new StreamCorruptedException("File map failed.");
            }
        }
    }

    private static void unMapBuffer(MappedByteBuffer buffer, Class channelClass) {
        if (buffer != null) {
            try {
                Method unmap = channelClass.getDeclaredMethod("unmap", MappedByteBuffer.class);
                unmap.setAccessible(true);
                unmap.invoke(channelClass, buffer);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                //TODO log
            }
        }
    }

    private static String hashFile(Long oid, String homePath) {
        return path(oid, homePath) + File.separator + oid + ".md5";
    }

    private static String file(Long oid, String homePath) {
        return path(oid, homePath) + File.separator + oid + ".dat";
    }

    private static String path(Long oid, String homePath) {
        return homePath + File.separator + oid;
    }
}
