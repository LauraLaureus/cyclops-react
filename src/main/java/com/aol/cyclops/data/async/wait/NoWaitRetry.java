package com.aol.cyclops.data.async.wait;

/**
 * Repeatedly retry to take or offer element to Queue if full or data unavailable
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class NoWaitRetry<T> implements WaitStrategy<T> {

    @Override
    public T take(final com.aol.cyclops.data.async.wait.WaitStrategy.Takeable<T> t) throws InterruptedException {
        T result;

        while ((result = t.take()) == null) {

        }

        return result;
    }

    @Override
    public boolean offer(final WaitStrategy.Offerable o) throws InterruptedException {
        while (!o.offer()) {

        }
        return true;
    }

}
