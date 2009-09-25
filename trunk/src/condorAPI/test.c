#include <stdio.h>

main(int argc, char ** argv){
  int i, j;
  int times = 10;
  int sum = 0;
  if (argc > 1)
	times = atoi(argv[1]);

  for (i = 0; i < 1000000; i++)
	for (j = 0; j < times; j++)
	  sum++;
  exit(0);
}
