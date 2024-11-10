package me.gabij.multiplebedspawn.models;

import org.apache.commons.lang.SerializationUtils;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class BedsDataType implements PersistentDataType<byte[], PlayerBedsData> {

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<PlayerBedsData> getComplexType() {
        return PlayerBedsData.class;
    }

    @Override
    public byte[] toPrimitive(PlayerBedsData complex, PersistentDataAdapterContext context) {
        return SerializationUtils.serialize(complex);
    }

    @Override
    public PlayerBedsData fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
        try {
            InputStream is = new ByteArrayInputStream(primitive);
            ObjectInputStream o = new CustomObjectInputStream(is);
            return (PlayerBedsData) o.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class CustomObjectInputStream extends ObjectInputStream {
        public CustomObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String className = desc.getName();
            String oldPackageName = getAsciiPackageName();
            if (className.startsWith(oldPackageName)) {
                className = className.replace(oldPackageName, "me.gabij.");
            }
            return super.resolveClass(ObjectStreamClass.lookup(Class.forName(className)));
        }

        private String getAsciiPackageName() {
            // old name is stored as ints
            int[] asciiValues = {109, 101, 46, 103, 97, 98, 114, 105, 101, 108, 102, 106, 46};
            StringBuilder sb = new StringBuilder();
            for (int val : asciiValues) {
                sb.append((char) val);
            }
            return sb.toString();
        }
    }

}
