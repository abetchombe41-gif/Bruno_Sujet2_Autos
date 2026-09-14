package com.app.util;

import com.app.model.Annonce;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortingAlgorithms {

    // Algorithme 1: Tri à Bulles 
    public static void bubbleSort(List<Annonce> list, Comparator<Annonce> comparator) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (comparator.compare(list.get(j), list.get(j + 1)) > 0) {
                    Annonce temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    // Algorithme 2: Tri Fusion
    public static void mergeSort(List<Annonce> list, Comparator<Annonce> comparator) {
        if (list.size() <= 1) return;
        int mid = list.size() / 2;
        List<Annonce> left = new ArrayList<>(list.subList(0, mid));
        List<Annonce> right = new ArrayList<>(list.subList(mid, list.size()));

        mergeSort(left, comparator);
        mergeSort(right, comparator);
        merge(list, left, right, comparator);
    }

    private static void merge(List<Annonce> list, List<Annonce> left, List<Annonce> right, Comparator<Annonce> comparator) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) list.set(k++, left.get(i++));
        while (j < right.size()) list.set(k++, right.get(j++));
    }
}
