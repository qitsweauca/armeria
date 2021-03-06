/*
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.client.retry;

import static com.linecorp.armeria.client.retry.FixedBackoff.NO_DELAY;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Control back off between attempts in a single retry operation.
 */
@FunctionalInterface
public interface Backoff {
    /**
     * Returns a {@link Backoff} that that will never wait between attempts.
     */
    static Backoff withoutDelay() {
        return NO_DELAY;
    }

    /**
     * Returns a {@link Backoff} that waits a fixed interval between attempts.
     */
    static Backoff fixed(long intervalMillis) {
        return new FixedBackoff(intervalMillis);
    }

    /**
     * Returns a {@link Backoff} that waits an exponentially-increasing amount of time between attempts.
     */
    static Backoff exponential(long minIntervalMillis, long maxIntervalMillis) {
        return exponential(minIntervalMillis, maxIntervalMillis, 2.0);
    }

    /**
     * Returns a {@link Backoff} that waits an exponentially-increasing amount of time between attempts.
     */
    static Backoff exponential(long minIntervalMillis, long maxIntervalMillis, double multiplier) {
        return new ExponentialBackoff(minIntervalMillis, maxIntervalMillis, multiplier);
    }

    /**
     * Returns the number of milliseconds to wait for before attempting a retry.
     *
     * @param numAttemptsSoFar the number of attempts made by a client so far, including the first attempt and
     *                         its following retries.
     *
     * @return the number of milliseconds to wait for before attempting a retry,
     *         or a negative value if no further retry has to be made.
     *
     * @throws IllegalArgumentException if {@code numAttemptsSoFar} is equal to or less than {@code 0}
     */
    long nextIntervalMillis(int numAttemptsSoFar);


    /**
     * Undecorates this {@link Backoff} to find the {@link Backoff} which is an instance of the specified
     * {@code backoffType}.
     *
     * @param backoffType the type of the desired {@link Backoff}
     * @return the {@link Backoff} which is an instance of {@code backoffType} if this {@link Backoff}
     *         decorated such a {@link Backoff}. {@link Optional#empty()} otherwise.
     */
    default <T> Optional<T> as(Class<T> backoffType) {
        requireNonNull(backoffType, "backoffType");
        return backoffType.isInstance(this) ? Optional.of(backoffType.cast(this))
                                            : Optional.empty();
    }

    /**
     * Returns a {@link Backoff} that provides an interval that increases using
     * <a href="https://www.awsarchitectureblog.com/2015/03/backoff.html">full jitter</a> strategy.
     */
    default Backoff withJitter(long minJitterMillis, long maxJitterMillis) {
        return withJitter(minJitterMillis, maxJitterMillis, ThreadLocalRandom::current);
    }

    /**
     * Returns a {@link Backoff} that provides an interval that increases using
     * <a href="https://www.awsarchitectureblog.com/2015/03/backoff.html">full jitter</a> strategy.
     */
    default Backoff withJitter(long minJitterMillis, long maxJitterMillis, Supplier<Random> randomSupplier) {
        return new JitterAddingBackoff(this, minJitterMillis, maxJitterMillis, randomSupplier);
    }

    /**
     * Returns a {@link Backoff} which limits the number of attempts up to the specified value.
     */
    default Backoff withMaxAttempts(int maxAttempts) {
        return new AttemptLimitingBackoff(this, maxAttempts);
    }
}
