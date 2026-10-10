package com.capitec.interfaceapp.runtime;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public final class ValueFormatter {
    private ValueFormatter() { }

    public static String format(Object value) {
        return format(value, 0);
    }

    public static String compactFormat(Object value) {
        return compact(format(value), value);
    }

    private static String format(Object value, int depth) {
        if (value == null) {
            return "null";
        }
        if (depth > 2) {
            return String.valueOf(value);
        }
        Class<?> type = value.getClass();
        if (isSimple(type)) {
            return String.valueOf(value);
        }
        if (type.isArray()) {
            return arrayText(value, depth);
        }
        if (value instanceof Collection) {
            return collectionText((Collection<?>) value, depth);
        }
        if (value instanceof Map) {
            return mapText((Map<?, ?>) value, depth);
        }
        Field[] fields = type.getFields();
        if (fields.length == 0 || hasCustomToString(type)) {
            return String.valueOf(value);
        }
        StringBuilder builder = new StringBuilder(type.getSimpleName()).append("{");
        boolean first = true;
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(field.getName()).append("=");
            try {
                builder.append(format(field.get(value), depth + 1));
            } catch (IllegalAccessException exception) {
                builder.append("<inaccessible>");
            }
        }
        return builder.append("}").toString();
    }

    private static boolean isSimple(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class
                || Enum.class.isAssignableFrom(type);
    }

    private static boolean hasCustomToString(Class<?> type) {
        try {
            return type.getMethod("toString").getDeclaringClass() != Object.class;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static String arrayText(Object value, int depth) {
        StringBuilder builder = new StringBuilder("[");
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(format(Array.get(value, i), depth + 1));
        }
        return builder.append("]").toString();
    }

    private static String collectionText(Collection<?> values, int depth) {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(format(value, depth + 1));
        }
        return builder.append("]").toString();
    }

    private static String mapText(Map<?, ?> values, int depth) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(format(entry.getKey(), depth + 1))
                    .append("=")
                    .append(format(entry.getValue(), depth + 1));
        }
        return builder.append("}").toString();
    }

    private static String compact(String text, Object value) {
        if (text == null || text.length() <= 180) {
            return text == null ? "" : text;
        }
        Class<?> type = value == null ? null : value.getClass();
        if (type != null && type.isArray()) {
            int length = Array.getLength(value);
            StringBuilder builder = new StringBuilder("[");
            int visible = Math.min(length, 4);
            for (int i = 0; i < visible; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(format(Array.get(value, i), 1));
            }
            if (length > visible) {
                builder.append(", ... total ").append(length).append(" items");
            }
            return builder.append("]").toString();
        }
        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;
            StringBuilder builder = new StringBuilder("[");
            int index = 0;
            for (Object item : values) {
                if (index >= 4) {
                    break;
                }
                if (index > 0) {
                    builder.append(", ");
                }
                builder.append(format(item, 1));
                index++;
            }
            if (values.size() > index) {
                builder.append(", ... total ").append(values.size()).append(" items");
            }
            return builder.append("]").toString();
        }
        return text.substring(0, 177) + "...";
    }
}
