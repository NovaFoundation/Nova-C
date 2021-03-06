#ifndef MEMORY_MANAGER_H
#define MEMORY_MANAGER_H

/**
 * Tells the memory manager to boot up and start managing the memory.
 */
void memoryManagerStart();

/**
 * Tells the memory manager to shutdown and stop managing the memory.
 */
void memoryManagerStop();

/**
 * Tells the memory manager to check the memory status.
 */
void memoryManagerCheck();

#endif