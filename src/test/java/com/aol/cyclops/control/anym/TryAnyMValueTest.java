package com.aol.cyclops.control.anym;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.data.Mutable;

public class TryAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromTry(Try.success(10));
        none = AnyM.fromTry(Try.failure(new RuntimeException()));
    }
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        
        
        just.get();
        assertThat(capture.get(),equalTo(10));
    }
}
