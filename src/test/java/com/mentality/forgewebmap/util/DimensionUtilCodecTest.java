package com.mentality.forgewebmap.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DimensionUtilCodecTest {
 @Test void vanillaDimensionNamesRemainStable(){assertEquals("overworld",DimensionUtil.toWebName("minecraft:overworld"));assertEquals("the_nether",DimensionUtil.toWebName("minecraft:the_nether"));assertEquals("the_end",DimensionUtil.toWebName("minecraft:the_end"));}
 @Test void customDimensionEncodingIsReversible(){for(String id:new String[]{"moda:moon","modb:moon","moda:space/moon"})assertEquals(id,DimensionUtil.decodeCustomId(DimensionUtil.encodeCustomId(id)).orElseThrow());}
 @Test void customDimensionsWithSamePathDoNotCollide(){assertNotEquals(DimensionUtil.encodeCustomId("moda:moon"),DimensionUtil.encodeCustomId("modb:moon"));}
 @Test void malformedCustomDimensionEncodingIsRejected(){assertTrue(DimensionUtil.decodeCustomId("custom-%%%").isEmpty());assertTrue(DimensionUtil.decodeCustomId("custom-").isEmpty());assertTrue(DimensionUtil.decodeCustomId("custom-Li4vYmFk").isEmpty());}
 @Test void encodedDimensionCannotEscapeTileRoot(){assertTrue(DimensionUtil.decodeCustomId("custom-Li4vLi4vYmFk").isEmpty());}
 @Test void unknownDimensionDoesNotFallbackToOverworld(){assertThrows(IllegalArgumentException.class,()->DimensionUtil.fromWebName("not-a-dimension"));}
}
