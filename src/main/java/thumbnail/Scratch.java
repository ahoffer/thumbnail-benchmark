package thumbnail;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class Scratch {

    String lastTechnique;

    @Param({"AAA", "BBB"})
    String name;

    public static void main(String[] args) throws RunnerException {
        String simpleName = Scratch.class.getSimpleName();
        Options opt = new OptionsBuilder().include(simpleName)
                .forks(1)
                .warmupIterations(0)
                .measurementIterations(2)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void test() {
        System.gc();
        lastTechnique = "test";
    }

    @Benchmark
    public void otherThing() {
        System.gc();
        lastTechnique = "otherThing()";
    }

    @TearDown
    public void teardown() {
        System.out.println("TEST TEST TEST  -  " + name + " : " + lastTechnique);
        StackTraceElement[] stackTraceElements = Thread.currentThread()
                .getStackTrace();
        //        String last = stackTraceElements[stackTraceElements.length-1].getMethodName();
        //        String first = stackTraceElements[0].getMethodName();
        //        System.err.println("first=" +first);
        //        System.err.println("last=" +last);
    }
}
