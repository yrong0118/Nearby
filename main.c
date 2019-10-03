//  main.c
//  CSCI6221-C
//
//  Created by Yue Rong on 1/3/19.
//  Copyright © 2019 Yue Rong.
//

#include <stdio.h>
#include<stdlib.h>
#include <time.h>

int* createInHeap(int arrSize){
    return (int*)malloc(sizeof(int)*arrSize);
}
int* createInStack(int arrSize){
    int array[arrSize];
    return array;
}
int main() {
    
    clock_t start, finish;
    double opertationTime;
    int arrSize = 1000000;


    double beginHeap = clock();
    for(int i = 0;i < 2000000; i++){
        int * arrayHeap = createInHeap(arrSize);
        free(arrayHeap);
    }
    double endHeap = clock();
    opertationTime = endHeap-beginHeap;
    printf("createInHeap: %f\n",opertationTime);

    double beginStack = clock();
    for(int i = 0;i < 2000000; i++){
        int* arrayStack = createInStack(arrSize);
    }
    double endStack = clock();
    opertationTime = endStack-beginStack;
    printf("createInStack: %f\n",opertationTime);
}
