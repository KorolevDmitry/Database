package DatabaseBase.entities;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: deemo_000
 * Date: 10/27/13
 * Time: 3:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class WrappedKeyValueTest {

    //@Test
    public void SizeCheck_NullKeyNullValueDeleted(){
        //arrange
        long heapFreeSizeBefore = Runtime.getRuntime().freeMemory();

        //act
        WrappedKeyValue<StringSizable, StringSizable> item = new WrappedKeyValue<StringSizable, StringSizable>(null,null,true);
        long heapFreeSizeAfter = Runtime.getRuntime().freeMemory();

        //assert
        long expectedSize = heapFreeSizeAfter - heapFreeSizeBefore;
        assertEquals(expectedSize, item.Size.longValue());
    }

    //@Test
    public void SizeCheck_NotNullKeyValueDeleted(){
        //arrange
        long heapFreeSizeBefore = Runtime.getRuntime().freeMemory();

        //act
        StringSizable key = new StringSizable("key");
        StringSizable value = new StringSizable("value");
        WrappedKeyValue<StringSizable, StringSizable> item = new WrappedKeyValue<StringSizable, StringSizable>(key,value,true);
        long heapFreeSizeAfter = Runtime.getRuntime().freeMemory();

        //assert
        long expectedSize = heapFreeSizeAfter - heapFreeSizeBefore;
        assertEquals(expectedSize, item.Size.longValue());
    }
}
