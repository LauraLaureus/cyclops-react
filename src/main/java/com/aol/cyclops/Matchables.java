package com.aol.cyclops;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.AsMatchable;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MTuple4;
import com.aol.cyclops.control.Matchable.MTuple5;
import com.aol.cyclops.control.Matchable.MXor;
import com.aol.cyclops.control.Matchable.MatchableIterable;
import com.aol.cyclops.control.Matchable.MatchableObject;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.ReaderT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.control.monads.transformers.seq.CompletableFutureTSeq;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.ReaderTSeq;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
import com.aol.cyclops.control.monads.transformers.seq.TryTSeq;
import com.aol.cyclops.control.monads.transformers.seq.XorTSeq;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.control.monads.transformers.values.ReaderTValue;
import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.control.monads.transformers.values.StreamTValue;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.data.async.Adapter;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Topic;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.util.ExceptionSoftener;

/**
 * This class contains static methods for Structural Pattern matching
 * 
 * @author johnmcclean
 *
 */
public class Matchables {

    /**
     * Match on a single value 
     * @param v Value to match on
     * 
     * @return Structural Pattern matching API for that value (encompasing presence and absence / null)
     */
    public static <T1> MTuple1<T1> match(final T1 v) {
        return () -> Tuple.tuple(v);
    }

    public static <T1, T2> MTuple2<T1, T2> match2(final T1 t1, final T2 t2) {
        return () -> Tuple.tuple(t1, t2);
    }

    public static <T1, T2, T3> MTuple3<T1, T2, T3> match3(final T1 t1, final T2 t2, final T3 t3) {
        return () -> Tuple.tuple(t1, t2, t3);
    }

    public static <T1, T2, T3, T4> MTuple4<T1, T2, T3, T4> match4(final T1 t1, final T2 t2, final T3 t3, final T4 t4) {
        return () -> Tuple.tuple(t1, t2, t3, t4);
    }

    public static <T1> MTuple1<T1> supplier(final Supplier<T1> s1) {

        return () -> Tuple.tuple(s1.get());
    }

    public static <T1> MTuple1<T1> tuple1(final Tuple1<T1> t2) {
        return () -> t2;
    }

    public static <T1, T2> MTuple2<T1, T2> supplier2(final Supplier<T1> s1, final Supplier<T2> s2) {
        return () -> Tuple.tuple(s1.get(), s2.get());
    }

    public static <T1, T2> MTuple2<T1, T2> tuple2(final Tuple2<T1, T2> t2) {
        return () -> t2;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE> MTuple3<T1, T2, T3> tuple3(final Tuple3<T1, T2, T3> t3) {
        return () -> t3;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE> MTuple3<T1, T2, T3> supplier3(final Supplier<T1> s1,
            final Supplier<T2> s2, final Supplier<T3> s3) {
        return () -> Tuple.tuple(s1.get(), s2.get(), s3.get());
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE> MTuple4<T1, T2, T3, T4> tuple4(
            final Tuple4<T1, T2, T3, T4> t4) {
        return () -> t4;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE> MTuple4<T1, T2, T3, T4> supplier4(final Supplier<T1> s1,
            final Supplier<T2> s2, final Supplier<T3> s3, final Supplier<T4> s4) {
        return () -> Tuple.tuple(s1.get(), s2.get(), s3.get(), s4.get());
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE, T5 extends TYPE> MTuple5<T1, T2, T3, T4, T5> tuple5(
            final Tuple5<T1, T2, T3, T4, T5> t5) {
        return () -> t5;
    }

    public static <TYPE, T1 extends TYPE, T2 extends TYPE, T3 extends TYPE, T4 extends TYPE, T5 extends TYPE> MTuple5<T1, T2, T3, T4, T5> supplier5(
            final Supplier<T1> s1, final Supplier<T2> s2, final Supplier<T3> s3, final Supplier<T4> s4, final Supplier<T5> s5) {
        return () -> Tuple.tuple(s1.get(), s2.get(), s3.get(), s4.get(), s5.get());
    }

    public static <T> MatchableIterable<T> iterable(final Iterable<T> o) {
        return () -> o;
    }

    /**
     * Create a new matchable that will match on the fields of the provided Decomposable
     * 
     * @param o Decomposable to match on it's fields
     * @return new Matchable
     */
    public static <T extends Decomposable> MatchableObject<T> decomposable(final Decomposable o) {
        return AsMatchable.asMatchable(o);
    }

    /**
     * Create a matchable that matches on the provided Objects
     * 
     * @param o Objects to match on
     * @return new Matchable
     */
    public static <T> MatchableObject<T> listOfValues(final T... o) {
        return AsMatchable.asMatchable(Arrays.asList(o));
    }

    public static <T> MXor<Queue<T>, Topic<T>> adapter(final Adapter<T> adapter) {
        return adapter.matches();
    }

    public static <T1> MXor<T1, Throwable> future(final CompletableFuture<T1> future) {
        return () -> FutureW.of(future)
                            .toXor()
                            .swap();
    }

    public static <T1> MXor<T1, Throwable> future(final FutureW<T1> future) {
        return () -> future.toXor()
                           .swap();
    }

    public static <T1, X extends Throwable> MXor<T1, X> tryMatch(final Try<T1, X> match) {
        return () -> match.toXor()
                          .swap();
    }

    public static <T> Matchable.MatchableOptional<T> maybe(final Maybe<T> opt) {
        return opt;
    }

    public static <T> Matchable.MatchableOptional<T> maybe(final Value<T> opt) {
        return () -> opt.toOptional();
    }

    public static <T> Matchable.MatchableOptional<T> optional(final Optional<T> opt) {
        return Maybe.fromOptional(opt);
    }

    public static <T> MXor<AnyMValue<T>, AnyMSeq<T>> anyM(final AnyM<T> anyM) {
        return () -> anyM instanceof AnyMValue ? Xor.secondary((AnyMValue<T>) anyM) : Xor.primary((AnyMSeq<T>) anyM);
    }

    public static <T> MXor<MaybeTValue<T>, MaybeTSeq<T>> maybeT(final MaybeT<T> transformer) {
        return () -> transformer instanceof MaybeTValue ? Xor.secondary((MaybeTValue<T>) transformer) : Xor.primary((MaybeTSeq<T>) transformer);
    }

    public static <T> MXor<OptionalTValue<T>, OptionalTSeq<T>> optionalT(final OptionalT<T> transformer) {
        return () -> transformer instanceof OptionalTValue ? Xor.secondary((OptionalTValue<T>) transformer)
                : Xor.primary((OptionalTSeq<T>) transformer);
    }

    public static <T> MXor<EvalTValue<T>, EvalTSeq<T>> evalT(final EvalT<T> transformer) {
        return () -> transformer instanceof EvalTValue ? Xor.secondary((EvalTValue<T>) transformer) : Xor.primary((EvalTSeq<T>) transformer);
    }

    public static <ST, T> MXor<XorTValue<ST, T>, XorTSeq<ST, T>> xorT(final XorT<ST, T> transformer) {
        return () -> transformer instanceof XorTValue ? Xor.secondary((XorTValue<ST, T>) transformer) : Xor.primary((XorTSeq<ST, T>) transformer);
    }

    public static <T, X extends Throwable> MXor<TryTValue<T, X>, TryTSeq<T, X>> tryT(final TryT<T, X> transformer) {
        return () -> transformer instanceof TryTValue ? Xor.secondary((TryTValue<T, X>) transformer) : Xor.primary((TryTSeq<T, X>) transformer);
    }

    public static <T> MXor<FutureWTValue<T>, FutureWTSeq<T>> futureWT(final FutureWT<T> transformer) {
        return () -> transformer instanceof FutureWTValue ? Xor.secondary((FutureWTValue<T>) transformer) : Xor.primary((FutureWTSeq<T>) transformer);
    }

    public static <T> MXor<CompletableFutureTValue<T>, CompletableFutureTSeq<T>> completableFutureT(final CompletableFutureT<T> transformer) {
        return () -> transformer instanceof CompletableFutureTValue ? Xor.secondary((CompletableFutureTValue<T>) transformer)
                : Xor.primary((CompletableFutureTSeq<T>) transformer);
    }

    public static <T> MXor<ListTValue<T>, ListTSeq<T>> listT(final ListT<T> transformer) {
        return () -> transformer instanceof ListTValue ? Xor.secondary((ListTValue<T>) transformer) : Xor.primary((ListTSeq<T>) transformer);
    }

    public static <T> MXor<SetTValue<T>, SetTSeq<T>> setT(final SetT<T> transformer) {
        return () -> transformer instanceof SetTValue ? Xor.secondary((SetTValue<T>) transformer) : Xor.primary((SetTSeq<T>) transformer);
    }

    public static <T> MXor<StreamTValue<T>, StreamTSeq<T>> streamT(final StreamT<T> transformer) {
        return () -> transformer instanceof StreamTValue ? Xor.secondary((StreamTValue<T>) transformer) : Xor.primary((StreamTSeq<T>) transformer);
    }

    public static <T> MXor<StreamableTValue<T>, StreamableTSeq<T>> streamableT(final StreamableT<T> transformer) {
        return () -> transformer instanceof StreamableTValue ? Xor.secondary((StreamableTValue<T>) transformer)
                : Xor.primary((StreamableTSeq<T>) transformer);
    }

    public static <T, R> MXor<ReaderTValue<T, R>, ReaderTSeq<T, R>> readerT(final ReaderT<T, R> transformer) {
        return () -> transformer instanceof ReaderTValue ? Xor.secondary((ReaderTValue<T, R>) transformer)
                : Xor.primary((ReaderTSeq<T, R>) transformer);
    }

    public static <X extends Throwable> MTuple4<Class, String, Throwable, MatchableIterable<StackTraceElement>> throwable(final X t) {
        return supplier4(() -> t.getClass(), () -> t.getMessage(), () -> t.getCause(), () -> iterable(Arrays.asList(t.getStackTrace())));
    }

    /**
     * Break an URL down into
     * protocol, host, port, path, query
     * 
     * @param url
     * @return
     */
    public static MTuple5<String, String, Integer, String, String> url(final URL url) {
        return supplier5(() -> url.getProtocol(), () -> url.getHost(), () -> url.getPort(), () -> url.getPath(), () -> url.getQuery());
    }

    public static Matchable.AutoCloseableMatchableIterable<String> lines(final BufferedReader in) {

        return new Matchable.AutoCloseableMatchableIterable<>(
                                                              in, () -> in.lines()
                                                                          .iterator());
    }

    public static Matchable.AutoCloseableMatchableIterable<String> lines(final URL url) {

        final BufferedReader in = ExceptionSoftener.softenSupplier(() -> new BufferedReader(
                                                                                            new InputStreamReader(
                                                                                                                  url.openStream())))
                                                   .get();
        return new Matchable.AutoCloseableMatchableIterable<>(
                                                              in, () -> in.lines()
                                                                          .iterator());
    }

    /**
     * Pattern match on the contents of a File
     * <pre>
     * {@code 
     * String result = Matchables.lines(new File(file))
                                 .on$12___()
                                 .matches(c->c.is(when("hello","world"),then("correct")), otherwise("miss")).get();
                                  
       }
       </pre>
     * 
     * @param f File to match against
     * @return Matcher
     */
    public static Matchable.AutoCloseableMatchableIterable<String> lines(final File f) {
        final Stream<String> stream = ExceptionSoftener.softenSupplier(() -> Files.lines(Paths.get(f.getAbsolutePath())))
                                                       .get();
        return new Matchable.AutoCloseableMatchableIterable<>(
                                                              stream, () -> stream.iterator());
    }

    public static MatchableIterable<String> words(final CharSequence seq) {
        return iterable(Arrays.asList(seq.toString()
                                         .split(" ")));
    }

    public static MatchableIterable<Character> chars(final CharSequence chars) {
        final Iterable<Character> it = () -> chars.chars()
                                                  .boxed()
                                                  .map(i -> Character.toChars(i)[0])
                                                  .iterator();
        return () -> it;
    }

    public static <ST, PT> MXor<ST, PT> xor(final Xor<ST, PT> xor) {
        return () -> xor;
    }

    public static <T> MTuple2<Maybe<T>, ListX<T>> headAndTail(final Collection<T> col) {
        final HeadAndTail<T> ht = CollectionX.fromCollection(col)
                                             .headAndTail();
        return supplier2(() -> ht.headMaybe(), () -> ht.tail()
                                                       .toListX());
    }

    public static <K, V> ReactiveSeq<MTuple2<K, V>> keysAndValues(final Map<K, V> map) {
        return ReactiveSeq.fromIterable(map.entrySet())
                          .map(entry -> supplier2(() -> entry.getKey(), () -> entry.getValue()));
    }

    public static MTuple3<Integer, Integer, Integer> dateDDMMYYYY(final Date date) {
        final Date input = new Date();
        final LocalDate local = input.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalDate();
        return localDateDDMMYYYY(local);
    }

    public static MTuple3<Integer, Integer, Integer> dateMMDDYYYY(final Date date) {
        final Date input = new Date();
        final LocalDate local = input.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalDate();
        return localDateMMDDYYYY(local);
    }

    public static MTuple3<Integer, Integer, Integer> localDateDDMMYYYY(final LocalDate date) {
        return supplier3(() -> date.getDayOfMonth(), () -> date.getMonth()
                                                               .getValue(),
                         () -> date.getYear());
    }

    public static MTuple3<Integer, Integer, Integer> localDateMMDDYYYY(final LocalDate date) {
        return supplier3(() -> date.getMonth()
                                   .getValue(),
                         () -> date.getDayOfMonth(), () -> date.getYear());
    }

    /**
     * Structural pattern matching on a Date's hour minutes and seconds component
     * 
     * @param date Date to match on
     * @return Structural pattern matcher for hours / minutes / seconds
     */
    public static MTuple3<Integer, Integer, Integer> dateHMS(final Date date) {
        final Date input = new Date();
        final LocalTime local = input.toInstant()
                                     .atZone(ZoneId.systemDefault())
                                     .toLocalTime();
        return localTimeHMS(local);
    }

    public static MTuple3<Integer, Integer, Integer> localTimeHMS(final LocalTime time) {
        return supplier3(() -> time.getHour(), () -> time.getMinute(), () -> time.getSecond());
    }

    public static <T> MXor<BlockingQueue<T>, java.util.Queue<T>> blocking(final java.util.Queue<T> queue) {

        return () -> queue instanceof BlockingQueue ? Xor.<BlockingQueue<T>, java.util.Queue<T>> secondary((BlockingQueue) queue)
                : Xor.<BlockingQueue<T>, java.util.Queue<T>> primary(queue);
    }
}
