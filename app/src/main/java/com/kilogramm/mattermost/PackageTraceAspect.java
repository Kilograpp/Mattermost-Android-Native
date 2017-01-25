package com.kilogramm.mattermost;

import android.os.Trace;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class PackageTraceAspect {

    @Pointcut("execution(*.new(..))")
    public void anyConstructor() {}

    @Pointcut("execution(* com.kilogramm.mattermost..*(..))")
    public void anyMethod() {}

    @Pointcut("!within(com.example.perf.*)")
    public void notPerf() {}

    @Before("anyMethod() && notPerf()")
    public void doBefore(JoinPoint joinPoint) {
        Trace.beginSection(joinPoint.getSignature().toShortString());
        Log.d("PackageTraceAspect","start:"+joinPoint.getSignature().toShortString());
    }

    @After("anyMethod() && notPerf()")
    public void doAfter(JoinPoint joinPoint) {
        Trace.endSection();
    }
}