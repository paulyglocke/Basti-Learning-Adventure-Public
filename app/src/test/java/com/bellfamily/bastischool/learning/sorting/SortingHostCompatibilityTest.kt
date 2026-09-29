package com.bellfamily.bastischool.learning.sorting

import com.bellfamily.bastischool.learning.coloursort.ColourSort
import com.bellfamily.bastischool.learning.models.ContentId
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64

class SortingHostCompatibilityTest {
    @Test fun frozenColourV1JournalRetainsExactBytesAndState() {
        // CSR1 before shared-host extraction: one placed item, next selected with Help.
        val bytes=Base64.getDecoder().decode("Q1NSMQAAAAEAAAABAAAAAQANbGVnYWN5LWNvbG91cgAHRU5HTElTSAAAGG9iamVjdC5jb2xvdXJzLmJsdWVfYmFsbAAXb2JqZWN0LmNvbG91cnMucmVkX2JhbGwAGG9iamVjdC5jb2xvdXJzLmJsdWVfYmFsbAAYb2JqZWN0LmNvbG91cnMucmVkX2Jsb2NrABlvYmplY3QuY29sb3Vycy5ibHVlX2Jsb2NrAQAAAAEAAAAAAAAACmNvbG91ci5yZWQAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAsQlBSRwAAAAEAAAAA0PLfVbrBhiM76sJjTblaQbOVx0TBal5Kkn74vn3e7yAa72G/iMx7OC9AEQhL3TBZIQOUxghEVwkBLR0y928B+Q==")
        val (state,pending)=SortingHost.decode(ColourSort,bytes)
        assertTrue(state.placements[0].placed)
        assertTrue(state.placements[1].support.hint)
        assertEquals(ContentId("object.colours.blue_ball"),state.selected)
        assertArrayEquals(bytes,SortingHost.encode(ColourSort,state,pending))
    }
}
