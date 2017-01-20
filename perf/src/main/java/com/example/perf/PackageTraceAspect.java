package com.example.perf;

import android.os.Trace;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class PackageTraceAspect {

    @Pointcut("execution(*.new(..))")
    public void anyConstructor() {}

    @Pointcut("execution(* *.*(..))")
    public void anyMethod() {}

    @Pointcut("!within(com.example.perf.*)")
    public void notPerf() {}

    @Before("(anyConstructor() || anyMethod()) && notPerf()")
    public void doBefore(JoinPoint joinPoint) {
        Trace.beginSection(joinPoint.getSignature().toShortString());
    }

    @After("anyMethod() && notPerf()")
    public void doAfter(JoinPoint joinPoint) {
        Trace.endSection();
    }
}
