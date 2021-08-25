package io.netty.cases.chapter.demo3;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class PoolByteBufPerformanceTest {
    private static final long count = 1000000000;
    private static final int size = 16;

    public static void main(String[] args) {
//        unPoolTest();
        poolTest();
    }

    private static void unPoolTest() {
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            Unpooled.buffer(size).release();
        }
        System.out.println("Execute " + count + " times cost time: " + (System.currentTimeMillis() - beginTime));
    }

    private static void poolTest() {
        PooledByteBufAllocator allocator = new PooledByteBufAllocator(true);
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            allocator.heapBuffer(size).release();
        }
        System.out.println("Execute " + count + " times cost time : " + (System.currentTimeMillis() - beginTime));
    }
}
