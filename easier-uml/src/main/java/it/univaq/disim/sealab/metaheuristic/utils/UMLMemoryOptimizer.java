package it.univaq.disim.sealab.metaheuristic.utils;

import org.eclipse.uml2.common.util.CacheAdapter;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UMLMemoryOptimizer {

    private static Unsafe unsafe;


    private void modifyField(Object newFieldValue, String fieldName, Object classInstance)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        VarHandle MODIFIERS;

        field.setAccessible(true);

        var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        int mods = field.getModifiers();

        if (Modifier.isFinal(mods)) {
            MODIFIERS.set(field, mods & ~Modifier.FINAL);
        }

        try {
            Method createCache = classInstance.getClass().getDeclaredMethod("createCacheAdapter");
            createCache.setAccessible(true);
            field.set(classInstance, newFieldValue);
            System.gc();
            field.set(classInstance, createCache.invoke(classInstance));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanup() {
        org.eclipse.uml2.common.util.CacheAdapter ca = org.eclipse.uml2.common.util.CacheAdapter.getInstance();
        ca.clear();
        String field = System.getProperty("org.eclipse.uml2.common.util.CacheAdapter.ThreadLocal") == null ? "INSTANCE"
                : "THREAD_LOCAL";

        try {
            modifyField(null, field, ca);


            //            java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
            //            modifiersField.setAccessible(true);
            //            modifiersField.setInt(instance, instance.getModifiers() & ~Modifier.FINAL);
            //            instance.set(ca, null);


//            Method createCache = CacheAdapter.class.getDeclaredMethod("createCacheAdapter");
//            createCache.setAccessible(true);
//            instance.set(ca, createCache.invoke(ca));
//
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
                 e) {
            e.printStackTrace();
        }
    }

}
