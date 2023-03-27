package kz.kuz.http

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainFragment() : Fragment() {
    // поскольку нам будет необходим Интернет, в манифест мы добавили запрос на разрешение
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        // необходимо установить удержание фрагмента, чтобы поворот не приводил к повторному
        // созданию потока
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.toolbar_title)
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val executorService = Executors.newSingleThreadExecutor()
        // ExecutorService используется для того, чтобы запустить параллельный поток, по этому
        // потоку нужно будет запустить Интернет-подключение, без этого Андроид выдаст ошибку
        // Executor можно создать разными способами:
        // Executors.newSingleThreadExecutor() - создаёт один параллельный поток
        // Executors.newSingleThreadExecutor(ThreadFactory threadFactory) - создаёт один
        // параллельный поток с подвязанной фабрикой
        // Executors.newFixedThreadPool(число линий) - создаёт один параллельный поток с указанным
        // числом линий, присваивая разные значения разным потокам можно управлять их мощностью
        // Executors.newFixedThreadPool(число линий, ThreadFactory threadFactory) - один
        // параллельный поток с указанным числом линий с подвязанной фабрикой
        // также существуют другие методы ExecutorService
        executorService.execute(Runnable
        // созданный ExecutorService можно запустить также разными способами:
        // execute(Runnable) - простая очередь операций
        // submit(Runnable) - после завершения работы выдаёт объект Future, после вызова
        // которого можно продолжить выполнение кода, он реализуется следующим способом:
        // Future future = executorService.submit(new Runnable() { ...
        // В главном потоке нужно потом вызвать метод future.get();
        // после которого можно продолжить программу
        // submit(Callable) - после завершения работы может отправить какое-то значение,
        // также в отличие от Runnable, Callable споссобен выдать Exception
        // Future future = executorService.submit(new Callable() {
        //     public Object call() throws Exception {
        //       ...
        //       return "значение";
        //     }
        // });
        // <?> value = future.get();
        // invokeAny(callables) - запускает несколько Callable и возвращает результат первого
        // завершённого потока, при этом все остальные потоки уничтожаются
        // также все потоки будут уничтожены, если первый завершившийся поток выдаст Exception
        // ExecutorService executorService = Executors.newSingleThreadExecutor();
        // Set<Callable<String>> callables = new HashSet<Callable<String>>();
        // callables.add(new Callable<String>() {
        //     public String call() throws Exception {
        //       return "Task1";
        //     }
        // });
        // callables.add(new Callable<String>() {
        //     public String call() throws Exception {
        //       return "Task2";
        //     }
        // });
        // callables.add(new Callable<String>() {
        //     public String call() throws Exception {
        //       return "Task3";
        //     }
        // });
        // String result = executorService.invokeAny(callables);
        // invokeAll(callables) - запускает несколько Callable и возвращает все результаты
        // в виде листа объектов Future
        // при этом если хотя хотя бы один из потоков вернёт Exception, это будет общим итогом
        // ExecutorService executorService = Executors.newSingleThreadExecutor();
        // Set<Callable<String>> callables = new HashSet<Callable<String>>();
        // callables.add(new Callable<String>() {
        //     public String call() throws Exception {
        //       return "Task1";
        //     }
        // });
        // callables.add(new Callable<String>() {
        //     public String call() throws Exception {
        //       return "Task2";
        //     }
        // });
        // callables.add(new Callable<String>() {
        //     public String call() throws Exception {
        //       return "Task3";
        //     }
        // });
        // List<Future<String>> futures = executorService.invokeAll(callables);
        // for (Future<String> future : futures) {
        //     String pooh = future.get();
        // }
        {
            var url: URL? = null
            try {
                url = URL("https://spice.101.kz/almaty.txt")
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            var connection: HttpURLConnection? = null
            try {
                connection = url?.openConnection() as HttpURLConnection
                // объект HttpURLConnection представляет подключение, но связь с конечной точкой
                // будет установлена только после вызова getInputStream()
                // или getOutputStream() для POST-вызовов
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val out = ByteArrayOutputStream()
            // ByteArrayOutputStream - класс, работающий с поступившими байтами
            var `in`: InputStream? = null
            // InputStream - класс, работающий с байтами по мере их доступности
            try {
                `in` = connection?.inputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                if (connection?.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException(connection?.responseMessage +
                            ": with https://spice.101.kz/almaty.txt")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            var bytesRead = 0 // переменная для записи количества байтов
            val buffer = ByteArray(1024) // создаём пустой массив buffer размером 1 кБайт
            while (true) {
                try {
                    if ((`in`!!.read(buffer).also { bytesRead = it }) <= 0) break
                    // in.read(buffer) - чтение данных из in и их запись в buffer
                    // bytesRead = in.read(buffer) - запись количества считанных байтов в
                    // переменную bytesRead
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                out.write(buffer, 0, bytesRead)
                // запись в out из массива buffer байтов в количестве bytesRead, начиная с
                // нулевого (то есть первого)
            }
            try {
                out.close() // завершаем запись в out
            } catch (e: IOException) {
                e.printStackTrace()
            }
            connection?.disconnect()
            val urlBytes = out.toByteArray()
            // toByteArray - метод класса ByteArrayOutputStream, который создаёт массив из
            // байтов
            val urlString = String(urlBytes) // создаём строку из массива байтов
            Log.e("data from site", urlString)
        })
        executorService.shutdown() // в конце ExecutorService нужно закрыть
        // если поток ещё не запущен, то его можно отменить: future.cancel()
        // можно закрыть executorService экстренно: executorService.shutdownNow();
        // в этом случае все неначатые потоки будут отменены
        // насчёт начатых потоков неизвестно, они будут либо прекращены, либо отработают до конца
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        // команда awaitTermination блокирует потоки до тех пор, пока все они не завершатся,
        // либо пока не пройдёт установленное время (в этом примере 60 сек), либо до Exception
        // обычно команда вызывается после shutdown(), либо shutdownNow()
        // данная команда может привести к утечке памяти, например, активность продолжает
        // существовать тогда, когда должна быть уничтожена
        return view
    }
}