# CRITICAL EXECUTION PATHS

## 1. Library Initialization
**Trigger**: Host application `onCreate()`
**Path**:
1.  `com.azikar24.wormaceptorapp.App.onCreate()`
2.  `com.azikar24.wormaceptor.WormaCeptor.init(context, storage, ...)`
3.  `org.koin.core.context.loadKoinModules(...)` (Dynamic registration of `transactionDao`)
4.  `WormaCeptor.logUnexpectedCrashes()` (Global exception handler hijack)

## 2. Network Traffic Interception
**Trigger**: OkHttp Request execution
**Path**:
1.  `okhttp3.RealCall.execute()` (or `enqueue`)
2.  `com.azikar24.wormaceptor.WormaCeptorInterceptor.intercept(chain)`
3.  `createTransactionFromRequest(request)` -> `storage.transactionDao.insertTransaction()`
4.  `chain.proceed(request)` (Actual network call)
5.  `updateTransactionFromResponse(transaction, response, ...)` -> `storage.transactionDao.updateTransaction()`
6.  `NotificationHelper.show()` (UI notification)

## 3. Crash Logging
**Trigger**: Uncaught Exception in any thread
**Path**:
1.  `Thread.UncaughtExceptionHandler` (Hijacked handler)
2.  `WormaCeptor.logUnexpectedCrashes` lambda executed
3.  StackTrace collection loop
4.  `storage.transactionDao.insertCrash(CrashTransaction)`
5.  Invoke `oldHandler.uncaughtException(...)` (Original system/host handler)

## 4. Launching the Inspector UI
**Trigger**: Device Shake or App Shortcut
**Path**:
1.  `ShakeDetector.onSensorChanged()` -> `WormaCeptor.startActivityOnShake` callback trigger.
2.  `context.startActivity(WormaCeptor.getLaunchIntent())`
3.  `com.azikar24.wormaceptor.internal.ui.mainactivity.WormaCeptorMainActivity.onCreate()`
4.  `Compose NavHost` resolution of `Route.NetworkList`
5.  `WormaCeptorViewModel` loads data via `TransactionDao.getAllTransactions()`

## 5. Data Retention Cleanup
**Trigger**: Scheduled Task (WorkManager)
**Path**:
1.  `androidx.work.Worker` execution
2.  `com.azikar24.wormaceptor.internal.support.RetentionWorker.doWork()`
3.  Read retention period from `SharedPreferences`
4.  `storage.transactionDao.deleteTransactionsBefore(thresholdDate)`
