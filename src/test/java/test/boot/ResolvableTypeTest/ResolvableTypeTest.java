package test.boot.ResolvableTypeTest;

import org.junit.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * @author JASONJ
 * @dateTime: 2021-05-27 17:04:09
 * @description: test
 */
public class ResolvableTypeTest {
    final HashMap<String, List<String>> map = new HashMap<>();
    @Test
    public void test() throws NoSuchFieldException {
        final ResolvableType re = ResolvableType.forField(getClass().getDeclaredField("map"));
        System.out.println(re.getSuperType());
        System.out.println(re.asMap());
        System.out.println(re.getGeneric(0).resolve());
        System.out.println(re.getGeneric(1).resolve());
        System.out.println(re.getGeneric(1));
        System.out.println(re.getGeneric(1,0));
    }

    @Test
    public void test2(){
        final ResolvableType resolvableType = ResolvableType.forType(List.class);
        System.out.println(resolvableType.resolve()); // 就是拿到底层的原始对象
        System.out.println(resolvableType.getType()); // 等价于 resolve()
        System.out.println(resolvableType.getSource()); // 根据typeProvider 进行解析,没有等价于 resolve
        System.out.println(resolvableType.getRawClass());//

        // -----------------------
        System.out.println(resolvableType.getGeneric(0));
    }


    class A<T> {
        private T t;
    }

    class B extends A<String>{
        public <T>  B(){

        }
    }

    class C{
        private A<String> a;
    }

    @Test
    public void test1() throws NoSuchFieldException {
        final B b = new B();
        System.out.println(Arrays.toString(((ParameterizedType) b.getClass().getGenericSuperclass()).getActualTypeArguments()));
        System.out.println(C.class.getDeclaredField("a").getGenericType());
    }

    // 这段代码算是失败的,因为无法获取ParameterizeType
    @Test
    public void test3(){

        // 什么是TypeVariable

        class A<T> {

        }
        Class<?> type = A.class;
        if( ParameterizedType.class.isAssignableFrom(type)){
            System.out.println("parameterizedType: " + A.class.getTypeName());
        }
        else if(TypeVariable.class.isAssignableFrom(type)){
            System.out.println("typeVariable: "+A.class.getTypeName());
        }
        else {
            System.out.println("class: "+A.class.getTypeName());
        }

        // 获取一个parameterizedType
        // 这种并不算是一个参数化类型
        List<String> value = new ArrayList<String>();

        // 获取的是TypeVariable
        System.out.println(Arrays.toString(value.getClass().getTypeParameters()));

        // 获取参数化类型

        if (ParameterizedType.class.isAssignableFrom(value.getClass())) {
            System.out.println(ParameterizedType.class.cast(value.getClass()).getActualTypeArguments());
        }


    }

    @Test
    public void test4() throws NoSuchFieldException {
        //获取 ParameterizeType
        class A<T> {

        }
        class B extends A<String>{
            private List<String> values;
        }

        System.out.println(Arrays.toString(((ParameterizedType) B.class.getGenericSuperclass()).getActualTypeArguments()));

        System.out.println(Arrays.toString(((ParameterizedType) B.class.getDeclaredField("values").getGenericType()).getActualTypeArguments()));

        final ParameterizedType values = (ParameterizedType)B.class.getDeclaredField("values").getGenericType();
        // 获取RawType
        System.out.println(values.getRawType());
        // 获取OwnType
        System.out.println(values.getOwnerType());
    }


    private final  Map<String,List<Integer>> map1 = new HashMap<>();

    private final ResolvableTypeProvider provider = new ResolvableTypeProvider() {
        @Override
        public ResolvableType getResolvableType() {
            return ResolvableType.forClassWithGenerics(Map.class,ResolvableType.forClass(String.class),ResolvableType.forClassWithGenerics(List.class,Integer.class));
        }
    };
    @Test
    public void test5(){
        Map<String,List<Integer>> map = new HashMap<>();

        final ResolvableType resolvableType = ResolvableType.forInstance(map);

        // 由此也可以得出,Spring同样无法获取当前局部对象的泛型变量实体类型
        System.out.println(Arrays.toString(resolvableType.getGenerics()));

       // forInstance 一般是为了动态传递实际的泛型参数
        // 正确使用方式应该如下:
        final ResolvableType resolvableType1 = ResolvableType.forInstance(provider);
        System.out.println(Arrays.toString(resolvableType1.getGenerics()));
    }

    static  class Aaa{

        public void test(String value){

        }
    }
    @Test
    public void test6() throws NoSuchMethodException {

        final ResolvableType test = ResolvableType.forMethodParameter(Aaa.class.getMethod("test",String.class), 0);

        System.out.println(test.getType());
    }

    @Test
    public void test7(){

        ResolvableType.forType()
    }
}
