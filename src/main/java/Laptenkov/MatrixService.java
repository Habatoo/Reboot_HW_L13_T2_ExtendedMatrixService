package Laptenkov;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Класс {@link MatrixService} для суммирования элементов
 * матрицы.
 * @author habatoo.
 *
 * Критерии приемки:
 *     Декомпозированные задачи ColumnSummator запускать в ExecutorService.
 *     Запрещено использовать wait-notify
 *     Метод sum должен быть покрыт unit тестами.
 */
public class MatrixService {

    /**
     * Внутрений класс {@link ColumnSummator}
     * для итерирования по колонкам переданной матрицы
     * и расчета суммы элементов.
     */
    private static class ColumnSummator implements Callable<Integer> {

        private int fromColumn;
        private int toColumn;
        private int[][] matrix;

        /**
         * Конструктор объекта {@link ColumnSummator} с полным перечнем
         * параметров.
         *
         * @param fromColumn стартовая колонка матрицы, с которой начинается суммирование
         * @param toColumn   конечная колонка матрицы, на которой заканчивается суммирование,
         *                   суммирование продолжается строго после колонки с индексом toColumn
         * @param matrix     матрица для суммирования
         */
        public ColumnSummator(int fromColumn, int toColumn, int[][] matrix) {
            this.fromColumn = fromColumn;
            this.toColumn = toColumn;
            this.matrix = matrix;
        }

        /**
         * Метод {@link ColumnSummator#call()} объекта {@link ColumnSummator}
         * реализует логику суммирования элементов двумерной марицы.
         */
        @Override
        public Integer call() {
            Integer sum = 0;
            for (; fromColumn <= toColumn; fromColumn++) {
                for (int j = 0; j < matrix.length; j++) {
                    sum = sum + matrix[j][fromColumn];
                }
            }
            return sum;
        }
    }

    /**
     * Метод {@link ColumnSummator#sum(int[][], int)} объекта {@link ColumnSummator}
     * находит сумму элементов в двумерном массиве.
     * Метод sum позволяет вычислять сумму с использованием nthreads потоков.
     * За счет этого достигается повышение производительности.
     * Сигнатура метода:
     * int sum(int[][] matrix, int nthreads)
     * Метод sum декомпозирует работу путем запуска задач ColumnSummator,
     * которые реализуют интерфейс Callable<Integer> Задача ColumnSummator
     * суммирует элементы матрицы в определенном диапазоне столбцов.
     *
     * @param matrix   - matrix
     * @param nthreads - threads count. It is guarantee that number of matrix column is greater than nthreads.
     * @return sum of matrix elements
     */
    public int sum(int[][] matrix, int nthreads) {

        if (matrix.length == 0) {
            return 0;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(nthreads);
        List<Future> futureList = new ArrayList<>();

        int numberOfParts = matrix.length / nthreads;
        int fromColumn = 0;
        int toColumn = fromColumn + numberOfParts - 1;

        for (int i = 0; i < nthreads; i++) {
            ColumnSummator columnSummator = new ColumnSummator(
                    fromColumn,
                    toColumn,
                    matrix);

            futureList.add(executorService.submit(columnSummator));

            fromColumn = toColumn + 1;
            if (i == nthreads - 2) {
                toColumn = matrix[0].length - 1;
            } else {
                toColumn = fromColumn + numberOfParts - 1;
            }
        }


        int sum = 0;
        for (Future future: futureList) {
            try {
                sum = sum + (Integer) future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        return sum;
    }
}