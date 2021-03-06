package com.aol.cyclops.control.transformers.seq;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class EvalTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        ListX<Eval<T>> list = ListX.<T>of(elements).map(Eval::now);
        return EvalT.fromIterable(list);
    }

    @Override
    public <T> Traversable<T> empty() {
        return EvalT.emptyList();
    }
   
}
