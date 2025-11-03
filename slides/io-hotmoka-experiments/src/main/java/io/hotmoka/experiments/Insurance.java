/*
Copyright 2021 Fausto Spoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.hotmoka.experiments;

import static io.takamaka.code.lang.Takamaka.require;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Payable;
import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Takamaka;
import io.takamaka.code.util.SnapshottableStorageSet;
import io.takamaka.code.util.SnapshottableStorageTreeSet;

public class Insurance extends Contract {
	public final static long MIN = 1_000, MAX = 1_000_000_000;
	private final Contract oracle;
	private final SnapshottableStorageSet<InsuredDay> insuredDays = new SnapshottableStorageTreeSet<>();

	public @FromContract @Payable Insurance(BigInteger amount, Contract oracle) {
		this.oracle = oracle;
	}

	public @FromContract(PayableContract.class) @Payable void buy(long amount, int day, int month, int year, int duration) {
		require(duration >= 1, "you must insure at least one day");
		require(duration <= 7, "you cannot insure more than a week");
		require(amount >= MIN * duration,
				() -> "we insure for at least " + MIN + " units of coin per day");
		require(amount <= MAX * duration,
				() -> "we insure for up to " + MAX + " units of coin per day");
		// if the date is wrong, this generates an exception
		var start = LocalDate.of(year, month, day);
		PayableContract payer = (PayableContract) caller();
		for (int offset = 0; offset < duration; offset++)
			insuredDays.add(new InsuredDay(payer, amount / duration, start.plusDays(offset)));
	}

	public @FromContract void itRains() {
		require(caller() == oracle, "only the oracle can call this method");

		// pay who insured today
		insuredDays.forEach(insuredDay -> {
			if (insuredDay.isToday())
				insuredDay.payer.receive(insuredDay.indemnization());
		});

		// clean-up the set of insured days
		insuredDays.snapshot().forEach(insuredDay -> {
			if (insuredDay.isTodayOrBefore())
				insuredDays.remove(insuredDay);
		});
	}

	private static class InsuredDay extends Storage {
		private final PayableContract payer;
		private final long amount;
		private final int day, month, year;

		private InsuredDay(PayableContract payer, long amount, LocalDate when) {
			this.payer = payer;
			this.amount = amount;
			this.day = when.getDayOfMonth();
			this.month = when.getMonthValue();
			this.year = when.getYear();
		}

		private boolean isToday() {
			return LocalDate.of(year, month, day).isEqual(today());
		}

		private boolean isTodayOrBefore() {
			return !LocalDate.of(year, month, day).isAfter(today());
		}

		private static LocalDate today() {
			var now = Instant.ofEpochMilli(Takamaka.now());
			return LocalDate.ofInstant(now, ZoneId.of("Europe/Rome"));
		}

		private long indemnization() {
			var season = new Season(LocalDate.of(year,  month,  day));
			if (season.isWinter())
				return amount * 18 / 10; // 180%
			else if (season.isSpring())
				return amount * 30 / 10; // 300%
			else if (season.isSummer())
				return amount * 50 / 10; // 500%
			else
				/* FALL */
				return amount * 28 / 10; // 280%
		}
	}
}