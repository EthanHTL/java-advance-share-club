package class01;

/**

 * 最简单的几种排序方法
 * 选择
 * 冒泡
 * 插入
 * 希尔
 * 归并
 * 堆排序
 */
public class SortUtil {


    /*--------------------------------------------------------*/
    // 选择排序  O(n2)
    // 在 i...N-1 的位置上选择最小的放到最前面
    public void selectionSort(int[] arr) {
        if (arr == null && arr.length < 2) {
            return;
        }
        for (int i = 0; i < arr.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < arr.length; j++) {
                minIndex = arr[j] < arr[minIndex] ? j : minIndex;
            }
            swap(arr, i, minIndex);
        }
    }

    /*--------------------------------------------------------*/
    // 冒泡排序  O(n2)
    // 进行迭代遍历，将较大的数推到最后面
    public void bubbleSort(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        // 0 ~ e 的范围上进行遍历
        for (int e = arr.length - 1; e > 0; e--) {
            for (int i = 0; i < e; i++) {
                if (arr[i] > arr[i + 1]) {
                    swap(arr, i, i + 1);
                }
            }
        }
    }

    /*--------------------------------------------------------*/
    // 插入排序： O(n2)
    // 1、从左到右逐步加入数， 做到 0 ~ i 上有序
    // 2、将新的数直接插入到正确的位置上
    public void insertionSort(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        // 不只1个数
        for (int i = 1; i < arr.length; i++) { // 0 ~ i 做到有序
            for (int j = i - 1; j >= 0 && arr[j] > arr[j + 1]; j--) { // 这里的arr[j+1] 就是第i个数, 将 arr[i] 插入到正确的位置上
                swap(arr, j, j + 1);
            }
        }
    }


    /*--------------------------------------------------------*/
    /* 希尔排序  O(n3/2)
     称为增量序列,把记录按下标的一定增量分组，对每组使用直接插入排序算法排序；
     随着增量逐渐减少，每组包含的关键词越来越多，当增量减至1时，整个文件恰被分成一组，算法便终止。*/
    public void hillSort1(int[] arr) {
        for (int gap = arr.length / 2; gap > 0; gap /= 2) {
            // i 从gap开始在 gap ~ n 上遍历
            // i++ ，依次读取然后加入到各自的组类进行排序
            for (int i = gap; i < arr.length; i++) {
                int j = i;
                while (j - gap >= 0 && arr[j] < arr[j - gap]) {
                    // 插入排序采用交换法
                    // 从后往前遍历，找到准确的位置
                    swap(arr, j, j - gap);
                    j -= gap;
                }
            }
        }
    }

    // 针对有序序列在插入时采用移动法。
    public void hillSort2(int[] arr) {
        //增量gap，并逐步缩小增量
        for (int gap = arr.length / 2; gap > 0; gap /= 2) {
            //从第gap个元素，逐个对其所在组进行直接插入排序操作
            for (int i = gap; i < arr.length; i++) {
                int j = i;
                int temp = arr[j];
                if (arr[j] < arr[j - gap]) {
                    while (j - gap >= 0 && temp < arr[j - gap]) {
                        //移动法
                        arr[j] = arr[j - gap];
                        j -= gap;
                    }
                    arr[j] = temp;
                }
            }
        }
    }


    /*--------------------------------------------------------*/
    // 归并排序  O(n*logn）
    //利用归并的思想实现的排序方法，该算法采用经典的分治（divide-and-conquer）策略（分治法将问题分(divide)成一些小的问题然后递归求解，而治(conquer)的阶段则将分的阶段得到的各答案"修补"在一起，即分而治之)。
    public void margeSort(int[] arr) {
        int n = arr.length;
        int[] temp = new int[n];
        margeSort(arr, 0, n - 1, temp);
    }

    private void margeSort(int a[], int L, int R, int temp[]) {
        if (L < R) {
            int mid = L + ((R - L) >> 1);
            margeSort(a, L, mid, temp);// 左有序
            margeSort(a, mid + 1, R, temp);// 右有序
            memoryArray(a, L, mid, R, temp);// L..R 上进行合并
        }
    }

    // 合并有序序列
    private void memoryArray(int[] a, int L, int mid, int R, int[] b) { // b[] 是为了避免合并过程中多余的空间开销
        int i = L;     //左序列指针
        int j = mid + 1; //右序列指针
        int k = 0; //临时数组指针
        while (i <= mid && j <= R) { // 进行组的合并操作
            // b[k++] = a[i] <= a[j] ? a[i++] : a[j++];
            if (a[i] <= a[j]) {
                b[k++] = a[i++];
            } else {
                b[k++] = a[j++];
            }

        }
        // 将多余的有序数依次插入在后面，两个while 只会执行一个
        while (i <= mid) { // 注意这里必须的等于，需要取到边界
            b[k++] = a[i++];
        }
        while (j <= R) {
            b[k++] = a[j++];
        }
        // 刷新数据
        for (i = 0; i < k; i++) {
            a[L + i] = b[i];
        }
    }


    /*--------------------------------------------------------*/
    /* 完全二叉树：除第 h 层外，其它各层 (1～h-1) 的结点数都达到最大个数
     堆排序 O(nlogn) 额外空间复杂度O(1)
       堆排序是一种选择排序，它的最坏，最好，平均时间复杂度均为O(nlogn)，它也是不稳定排序。
       堆是具有以下性质的完全二叉树：每个结点的值都大于或等于其左右孩子结点的值，称为 大顶堆 ；或者每个结点的值都小于或等于其左右孩子结点的值，称为 小顶堆。
       大顶堆：arr[i] >= arr[2i+1] && arr[i] >= arr[2i+2]
       小顶堆：arr[i] <= arr[2i+1] && arr[i] <= arr[2i+2]
        父节点： （i-1）/2
     步骤如下：
        a.将无需序列构建成一个堆，根据升序降序需求选择大顶堆或小顶堆;
     　　b.将堆顶元素与末尾元素交换，将最大元素"沉"到数组末端;
     　　c.重新调整结构，使其满足堆定义，然后继续交换堆顶元素与当前末尾元素，反复执行调整+交换步骤，直到整个序列有序。
     */
    public void heapSort(int[] arr) {
        //1.构建大顶堆
        for (int i = arr.length / 2 - 1; i >= 0; i--) {
            //从第一个非叶子结点从下至上，从右至左调整结构
            heapify(arr, i, arr.length);
        }
        //2.调整堆结构+交换堆顶元素与末尾元素
        for (int j = arr.length - 1; j > 0; j--) {
            swap(arr, 0, j);//将堆顶元素与末尾元素进行交换
            heapify(arr, 0, j);//重新对堆进行调整
        }
    }

    // 做到 0 ~ heapSize 为大根堆
    // arr[index]位置的数，能否往下移动
    public void heapify(int[] arr, int index, int heapSize) {
        int left = index << 1 | 1; // 左孩子的下标 等同于 index *2 + 1
        while (left < heapSize) { // 下方还有孩子的时候
            // 两个孩子中，谁的值大，把下标给largest
            // 1）只有左孩子，left -> largest
            // 2) 同时有左孩子和右孩子，右孩子的值<= 左孩子的值，left -> largest
            // 3) 同时有左孩子和右孩子并且右孩子的值> 左孩子的值， right -> largest
            int largest = left + 1 < heapSize && arr[left + 1] > arr[left] ? left + 1 : left;
            // 父和较大的孩子之间，谁的值大，把下标给largest
            largest = arr[largest] > arr[index] ? largest : index;
            if (largest == index) {
                break;
            }
            swap(arr, largest, index);
            index = largest;
            left = index << 1 | 1;
        }
    }

    private void heapify1(int[] arr, int index, int heapSize) {
        int L = index << 1 + 1;
        while (L < heapSize) {
            //     取最大的孩子下表
            int largest = L + 1 < heapSize && arr[L + 1] > arr[L] ? (L + 1) : arr[L];
            //    取最大值
            largest = arr[largest] > arr[index] ? largest : index;
            if (largest == index){
                break;
            }
            swap(arr,largest,index);
            index = largest;
            L = index << 1 + 1;
        }
    }


    /*--------------------------------------------------------*/
    // 使用位运算进行交换，如果i==j,则会出现问题
    private void swap(int[] arr, int i, int j) {


        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    // 位运算方式
    // arr[i] = arr[i] ^ arr[j];
    // arr[j] = arr[i] ^ arr[j];
    // arr[i] = arr[i] ^ arr[j];

    // 加减方式
    // arr[a] = arr[a]+arr[b];
    // arr[b] = arr[a]-arr[b];
    // arr[a] = arr[a]-arr[b];


}
