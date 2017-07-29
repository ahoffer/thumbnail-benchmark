STUFF PULLED TOGTHER FROM OTHER PEOPLE'S WORK I STUMBLED ACROSS AT SOME POINT OR ANOTHER:

Only Minor GC pauses cleaning the Young Generation are affected.  The frequency nor duration of the GC pauses cleaning the Old Generation is not directly impacted by allocation rate, but instead by promotion rate. A low allocation rate might mean Eden space is too small and is a bottle neck. Or a low allocation rate might indicate you are not creating many objects. 

Promotion rate is measured in the amount of data propagated from Young generation to Old generation per time unit. It is often measured in MB/sec, similar to allocation rate. But as opposed to allocation rate that affects the frequency of Minor GC events, promotion rate affects the frequency of Major GC events.

the more stuff you promote to Old generation the faster you fill it. Filling the Old gen faster means that the frequency of the GC events cleaning Old generation will increase. igh promotion rates can surface a symptom of a problem called premature promotion. Weak Generational Hypothesis is true when allocation rate >> promotion rate


Allocation Failure – Cause of the collection. In this case, the GC is triggered due to a data structure not fitting into any region in Young Generation.

Validating the quality of the measurements:

I wonder if we should fold this new profiler into -prof gc, that reports
churn rates. Users will then have a more complete picture of what is
going on:
 a) when allocation and churn rates match (even though churn rates will
have much larger errors), you can be arguably sure about the whole thing;
 b) when allocation rate is lower than churn rate, you know some
allocations are missing from the profiling, prompting the investigation;
 c) when allocation rate is higher than churn rate, you know there is
either a memory leak, or some other kind of problem.

Norm
"norm" is the derivative, not measured directly. The gc.alloc.rate.norm measures how many bytes are allocated per operation on average. Lower is better.

A high churn rate alone will slow down the applications because the JVM needs to execute young-generation GCs more frequently. Young-generation GCs are inexpensive only if most objects die! In a high-load situation with many concurrent transactions, many objects will be alive at the time of the GC. This leads to premature promotion and expensive major garbage collections down the road. 
