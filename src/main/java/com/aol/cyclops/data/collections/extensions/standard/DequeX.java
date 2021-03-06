package com.aol.cyclops.data.collections.extensions.standard;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.types.OnEmptySwitch;

/**
 * An eXtended Deque type, that offers additional eagerly executed functional style operators such as bimap, filter and more
 * 
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface DequeX<T> extends Deque<T>, MutableCollectionX<T>, OnEmptySwitch<T, Deque<T>> {

    /**
     * Create a DequeX that contains the Integers between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range DequeX
     */
    public static DequeX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .toDequeX();
    }

    /**
     * Create a DequeX that contains the Longs between start and end
     * 
     * @param start
     *            Number of range to start from
     * @param end
     *            Number for range to end at
     * @return Range DequeX
     */
    public static DequeX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .toDequeX();
    }

    /**
     * Unfold a function into a DequeX
     * 
     * <pre>
     * {@code 
     *  DequeX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty());
     * 
     * //(1,2,3,4,5)
     * 
     * }</pre>
     * 
     * @param seed Initial value 
     * @param unfolder Iteratively applied function, terminated by an empty Optional
     * @return DequeX generated by unfolder function
     */
    static <U, T> DequeX<T> unfold(final U seed, final Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .toDequeX();
    }

    /**
     * Generate a DequeX from the provided Supplier up to the provided limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param s Supplier to generate DequeX elements
     * @return DequeX generated from the provided Supplier
     */
    public static <T> DequeX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .toDequeX();
    }

    /**
     * Create a DequeX by iterative application of a function to an initial element up to the supplied limit number of times
     * 
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return DequeX generated by iterative application
     */
    public static <T> DequeX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .toDequeX();

    }

    /**
     * @return A Collector that generates a mutable Deque from a Collection
     */
    static <T> Collector<T, ?, Deque<T>> defaultCollector() {
        return Collectors.toCollection(() -> new ArrayDeque<>());
    }

    /**
     * @return An empty DequeX
     */
    public static <T> DequeX<T> empty() {
        return fromIterable((Deque<T>) defaultCollector().supplier()
                                                         .get());
    }

    /**
     * Construct a Deque from the provided values
     * 
     * <pre>
     * {@code 
     *     DequeX<Integer> deque = DequeX.of(1,2,3,4);
     * 
     * }</pre>
     *
     * 
     * 
     * @param values to construct a Deque from
     * @return DequeX
     */
    public static <T> DequeX<T> of(final T... values) {
        final Deque<T> res = (Deque<T>) defaultCollector().supplier()
                                                          .get();
        for (final T v : values)
            res.add(v);
        return fromIterable(res);
    }

    /**
     * Construct a DequeX with a single value
     * 
     * <pre>
     * {@code 
     *     DequeX<Integer> deque = DequeX.of(1);
     * 
     * }</pre>
     * 
     * 
     * @param value Single value
     * @return DequeX
     */
    public static <T> DequeX<T> singleton(final T value) {
        return of(value);
    }

    /**
     * Construct a DequeX from an Publisher
     * 
     * @param publisher
     *            to construct DequeX from
     * @return DequeX
     */
    public static <T> DequeX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>) publisher)
                          .toDequeX();
    }

    /**
     * Construct a DequeX from an Iterable
     * 
     * @param iterable
     *            to construct DequeX from
     * @return DequeX
     */
    public static <T> DequeX<T> fromIterable(final Iterable<T> it) {
        return fromIterable(defaultCollector(), it);
    }

    /**
     * Construct a Deque from the provided Collector and Iterable.
     * 
     * @param collector To construct DequeX from
     * @param it Iterable to construct DequeX
     * @return DequeX
     */
    public static <T> DequeX<T> fromIterable(final Collector<T, ?, Deque<T>> collector, final Iterable<T> it) {
        if (it instanceof DequeX)
            return (DequeX) it;
        if (it instanceof Deque)
            return new DequeXImpl<T>(
                                     (Deque) it, collector);
        return new DequeXImpl<T>(
                                 StreamUtils.stream(it)
                                            .collect(collector),
                                 collector);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
     */
    @Override
    default DequeX<T> toDequeX() {
        return this;
    }

    /**
     * @return The collector for this DequeX
     */
    public <T> Collector<T, ?, Deque<T>> getCollector();

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    default <T1> DequeX<T1> from(final Collection<T1> c) {
        return DequeX.<T1> fromIterable(getCollector(), c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    default <X> DequeX<X> fromStream(final Stream<X> stream) {
        return new DequeXImpl<>(
                                stream.collect(getCollector()), getCollector());
    }

    /**
     * Combine two adjacent elements in a DequeX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  DequeX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced DequeX
     */
    @Override
    default DequeX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (DequeX<T>) MutableCollectionX.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    default <R> DequeX<R> unit(final Collection<R> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    default <R> DequeX<R> unit(final R value) {
        return singleton(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    default <R> DequeX<R> unitIterator(final Iterator<R> it) {
        return fromIterable(() -> it);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#stream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
     */
    @Override
    default DequeX<T> reverse() {

        return (DequeX<T>) MutableCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> filter(final Predicate<? super T> pred) {

        return (DequeX<T>) MutableCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> map(final Function<? super T, ? extends R> mapper) {

        return (DequeX<R>) MutableCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> flatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (DequeX<R>) MutableCollectionX.super.flatMap(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    default DequeX<T> limit(final long num) {

        return (DequeX<T>) MutableCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    default DequeX<T> skip(final long num) {

        return (DequeX<T>) MutableCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> takeWhile(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
     */
    @Override
    default DequeX<T> takeRight(final int num) {
        return (DequeX<T>) MutableCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
     */
    @Override
    default DequeX<T> dropRight(final int num) {
        return (DequeX<T>) MutableCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> dropWhile(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> takeUntil(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> dropUntil(final Predicate<? super T> p) {
        return (DequeX<T>) MutableCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @SuppressWarnings("unchecked")
    @Override
    default <R> DequeX<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (DequeX<R>) MutableCollectionX.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    default DequeX<T> slice(final long from, final long to) {
        return (DequeX<T>) MutableCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int)
     */
    @Override
    default DequeX<ListX<T>> grouped(final int groupSize) {
        return (DequeX<ListX<T>>) MutableCollectionX.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> DequeX<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {
        return (DequeX) MutableCollectionX.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
     */
    @Override
    default <K> DequeX<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {
        return (DequeX) MutableCollectionX.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (DequeX) MutableCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> DequeX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (DequeX<R>) MutableCollectionX.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    default <U, R> DequeX<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (DequeX<R>) MutableCollectionX.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> DequeX<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (DequeX<R>) MutableCollectionX.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int)
     */
    @Override
    default DequeX<ListX<T>> sliding(final int windowSize) {
        return (DequeX<ListX<T>>) MutableCollectionX.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
     */
    @Override
    default DequeX<ListX<T>> sliding(final int windowSize, final int increment) {
        return (DequeX<ListX<T>>) MutableCollectionX.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default DequeX<T> scanLeft(final Monoid<T> monoid) {
        return (DequeX<T>) MutableCollectionX.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> DequeX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (DequeX<U>) MutableCollectionX.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default DequeX<T> scanRight(final Monoid<T> monoid) {
        return (DequeX<T>) MutableCollectionX.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> DequeX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (DequeX<U>) MutableCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> DequeX<T> sorted(final Function<? super T, ? extends U> function) {

        return (DequeX<T>) MutableCollectionX.super.sorted(function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
     */
    @Override
    default DequeX<T> plus(final T e) {
        add(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
     */
    @Override
    default DequeX<T> plusAll(final Collection<? extends T> list) {
        addAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
     */
    @Override
    default DequeX<T> minus(final Object e) {
        remove(e);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
     */
    @Override
    default DequeX<T> minusAll(final Collection<?> list) {
        removeAll(list);
        return this;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default DequeX<T> peek(final Consumer<? super T> c) {
        return (DequeX<T>) MutableCollectionX.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.CollectionX#patternMatch(java.lang.Object, java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {
        return (DequeX<R>) MutableCollectionX.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default DequeX<T> cycle(final int times) {

        return (DequeX<T>) MutableCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    default DequeX<T> cycle(final Monoid<T> m, final int times) {

        return (DequeX<T>) MutableCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (DequeX<T>) MutableCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (DequeX<T>) MutableCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (DequeX) MutableCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (DequeX) MutableCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> DequeX<Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return (DequeX) MutableCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> DequeX<Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return (DequeX) MutableCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default DequeX<Tuple2<T, Long>> zipWithIndex() {
        //
        return (DequeX<Tuple2<T, Long>>) MutableCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#distinct()
     */
    @Override
    default DequeX<T> distinct() {

        return (DequeX<T>) MutableCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#sorted()
     */
    @Override
    default DequeX<T> sorted() {

        return (DequeX<T>) MutableCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default DequeX<T> sorted(final Comparator<? super T> c) {

        return (DequeX<T>) MutableCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> skipWhile(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> skipUntil(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> limitWhile(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> limitUntil(final Predicate<? super T> p) {

        return (DequeX<T>) MutableCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default DequeX<T> intersperse(final T value) {

        return (DequeX<T>) MutableCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#shuffle()
     */
    @Override
    default DequeX<T> shuffle() {

        return (DequeX<T>) MutableCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#skipLast(int)
     */
    @Override
    default DequeX<T> skipLast(final int num) {

        return (DequeX<T>) MutableCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#limitLast(int)
     */
    @Override
    default DequeX<T> limitLast(final int num) {

        return (DequeX<T>) MutableCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default DequeX<T> onEmpty(final T value) {

        return (DequeX<T>) MutableCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default DequeX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (DequeX<T>) MutableCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> DequeX<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (DequeX<T>) MutableCollectionX.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    default DequeX<T> shuffle(final Random random) {

        return (DequeX<T>) MutableCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#permutations()
     */
    @Override
    default DequeX<ReactiveSeq<T>> permutations() {

        return (DequeX<ReactiveSeq<T>>) MutableCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#combinations(int)
     */
    @Override
    default DequeX<ReactiveSeq<T>> combinations(final int size) {

        return (DequeX<ReactiveSeq<T>>) MutableCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#combinations()
     */
    @Override
    default DequeX<ReactiveSeq<T>> combinations() {

        return (DequeX<ReactiveSeq<T>>) MutableCollectionX.super.combinations();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> DequeX<U> cast(final Class<? extends U> type) {

        return (DequeX<U>) MutableCollectionX.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> DequeX<U> ofType(final Class<? extends U> type) {

        return (DequeX<U>) MutableCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> filterNot(final Predicate<? super T> fn) {

        return (DequeX<T>) MutableCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    default DequeX<T> notNull() {

        return (DequeX<T>) MutableCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default DequeX<T> removeAll(final Stream<? extends T> stream) {

        return (DequeX<T>) MutableCollectionX.super.removeAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    default DequeX<T> removeAll(final Iterable<? extends T> it) {

        return (DequeX<T>) MutableCollectionX.super.removeAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default DequeX<T> removeAll(final T... values) {

        return (DequeX<T>) MutableCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    default DequeX<T> retainAll(final Iterable<? extends T> it) {

        return (DequeX<T>) MutableCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    default DequeX<T> retainAll(final Stream<? extends T> seq) {

        return (DequeX<T>) MutableCollectionX.super.retainAll(seq);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
     */
    @Override
    default DequeX<T> retainAll(final T... values) {

        return (DequeX<T>) MutableCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> DequeX<C> grouped(final int size, final Supplier<C> supplier) {

        return (DequeX<C>) MutableCollectionX.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (DequeX<ListX<T>>) MutableCollectionX.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (DequeX<ListX<T>>) MutableCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> DequeX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (DequeX<C>) MutableCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> DequeX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (DequeX<C>) MutableCollectionX.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default DequeX<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (DequeX<ListX<T>>) MutableCollectionX.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    default DequeX<T> removeAll(final Seq<? extends T> stream) {

        return (DequeX<T>) MutableCollectionX.super.removeAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    default DequeX<T> retainAll(final Seq<? extends T> stream) {

        return (DequeX<T>) MutableCollectionX.super.retainAll(stream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default DequeX<T> onEmptySwitch(final Supplier<? extends Deque<T>> supplier) {
        if (isEmpty())
            return DequeX.fromIterable(supplier.get());
        return this;
    }

    /**
     * Narrow a covariant Deque
     * 
     * <pre>
     * {@code 
     * DequeX<? extends Fruit> deque = DequeX.of(apple,bannana);
     * DequeX<Fruit> fruitDeque = DequeX.narrow(deque);
     * }
     * </pre>
     * 
     * @param setX to narrow generic type
     * @return SetX with narrowed type
     */
    public static <T> DequeX<T> narrow(final DequeX<? extends T> dequeX) {
        return (DequeX<T>) dequeX;
    }

}
