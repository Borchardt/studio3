#include <stdio.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <errno.h> 
#include <string.h>
#include <ctype.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/sysctl.h>
#include <sys/stat.h>

#ifdef LINUX 
#include <pty.h> 
#endif 

#ifdef __APPLE__
#include <stdint.h> 
#include <util.h> 
#endif

#ifndef MAXCOMLEN
#define MAXCOMLEN 255
#endif

#define ESC '\033'
#define DLE	'\020'

#define MAX_ESC_SEQUENCE_LENGTH	16

static void send_process_list(char* ptyname);

int 
main (int argc, char** argv) 
{
	/* Read char */
	char buffer[1024+1];
	int buffer_index = 0;
	int buffer_offset;
	int read_count;
		
	/* Descriptor set for select */
	fd_set descriptor_set;
	
	int pty;
	char ptyname[MAXCOMLEN+1];
	struct winsize size = { 0, 0 };

	if( argc > 1 ) {
		unsigned int width, height;
		if( sscanf(argv[1], "%ux%u", &width, &height) == 2 ) {
			size.ws_col = width;
			size.ws_row = height;
		}
	}
	
	switch (forkpty(&pty,	/* pseudo-terminal master end descriptor */ 
					ptyname,/* This can be a char[] buffer used to get... */ 
							/* ...the device name of the pseudo-terminal */ 
					NULL,	/* This can be a struct termios pointer used... */ 
							/* to set the terminal attributes */ 
					size.ws_col != 0 ? &size : NULL))	/* This can be a struct winsize pointer used... */ 
    {						/* ...to set the screen size of the terminal */ 
		case -1: /* Error */ 
			perror ("fork()"); 
			exit (EXIT_FAILURE); 
			
		case 0: /* This is the child process */ 
			execl("/bin/bash", "-bash", "-li", NULL); 
			
			perror("exec()"); /* Since exec* never return */ 
			exit (EXIT_FAILURE); 
			
		default: /* This is the parent process */
			while (1) 
			{ 
				FD_ZERO (&descriptor_set);
				FD_SET (STDIN_FILENO, &descriptor_set); 
				FD_SET (pty, &descriptor_set);
				
				if (select (FD_SETSIZE, &descriptor_set, NULL, NULL, NULL) < 0) 
				{ 
					perror ("select()"); 
					exit (EXIT_FAILURE); 
				}
				
				/* User typed something at STDIN */
				if (FD_ISSET (STDIN_FILENO, &descriptor_set)) 
				{
					read_count = read(STDIN_FILENO, &buffer[buffer_index], sizeof(buffer)-buffer_index-1);
					
					switch (read_count)
					{
						case -1:
							fprintf(stderr, "Disconnected.\n"); 
							exit (EXIT_FAILURE);
							break;
							
						case 0:
							fprintf (stderr, "Done\n"); 
							exit (EXIT_SUCCESS);
							break;
							
						default:
							buffer_index += read_count;
							buffer[buffer_index] = '\0';
							buffer_offset = 0;
							do {
								char *ch;
								int i = 0;
								
								ch = strchr(&buffer[buffer_offset], DLE);
								if (ch != NULL)
								{
									for( i = ch - buffer; (i < buffer_index) && !((buffer[i] >= 64) && (buffer[i] <= 126)); ++i)
									{
										/* noop */
									}
									if (i < buffer_index)
									{
										if( buffer[i] == ESC )
										{
											buffer_index -= i;
											memmove(ch, &buffer[i], buffer_index+1);
											--i;
										} else {
											if (strcmp(ch, "\020$p") == 0) {
												send_process_list(ptyname);
											}
											buffer_index -= i + 1;
											if (buffer_index > 0) {
												memmove(ch, &buffer[i+1], buffer_index+1);
												continue;
											}
										}
									}
								}
								ch = strchr(&buffer[buffer_offset], ESC);
								if (ch == NULL)
								{
									write(pty, &buffer, buffer_index);
									buffer_index = 0;
									break;
								}

								i = ch - buffer;
								if ((buffer_index - i < 2) || (buffer[i+1] != '['))
								{
									buffer_offset = i+1;
									continue;
								}
								if (i > 0) {
									write(pty, buffer, i);
									buffer_index -= i;
									memmove(buffer, &buffer[i], buffer_index+1);
									
								}
								for( i = 2; (i < buffer_index) && !((buffer[i] >= 64) && (buffer[i] <= 126)); ++i)
								{
									/* noop */
								}
								if (i < buffer_index)
								{
									int param[4];
									switch (buffer[i])
									{
										case ESC:
											write(pty, buffer, i);
											--i;
											break;
										case 't':
											if ((sscanf(&buffer[2], "%d;%d;%dt", &param[0], &param[1], &param[2]) == 3) && (param[0] == 8))
											{
												struct winsize size;
												size.ws_row = param[1];
												size.ws_col = param[2];
												ioctl(pty, TIOCSWINSZ, &size);
												break;
											}
										default:
											write(pty, buffer, i+1);
											break;
									}
									buffer_index -= i + 1;
									if (buffer_index > 0) {
										memmove(buffer, &buffer[i+1], buffer_index+1);
										continue;
									}
								} else {
									if (buffer_index > MAX_ESC_SEQUENCE_LENGTH) {
										write(pty, &buffer, buffer_index);
										buffer_index = 0;
									}
								}
								break;
							} while (1);
							break;
					}
				} 
				
				/* Output from the bash */
				if (FD_ISSET (pty, &descriptor_set)) 
				{
					read_count = read(pty, &buffer, sizeof(buffer)-1);
					
					switch (read_count)
					{
						case -1:
							fprintf (stderr, "Disconnected.\n"); 
							exit (EXIT_FAILURE); 
							break;
							
						case 0:
							fprintf (stderr, "Done\n"); 
							exit (EXIT_SUCCESS);
							break;
							
						default:
							write (STDOUT_FILENO, &buffer, read_count);
							break;
					}
				}
			}
    } 
	
	return EXIT_FAILURE; 
} 

static void
send_process_list(char* ptyname)
{
#ifdef __APPLE__
	struct stat sb;
	int mib[4] = { CTL_KERN, KERN_PROC, KERN_PROC_TTY, 0};
	int i;
	int kp_count = 0;
	struct kinfo_proc* kp = NULL;
	if (!stat(ptyname, &sb))
	{
		size_t len = 0;
		mib[3] = sb.st_rdev;
		if (!sysctl(mib, 4, NULL, &len, NULL, 0))
		{
			kp_count = len/sizeof(struct kinfo_proc);
			len = 2*kp_count*sizeof(struct kinfo_proc);
			kp = malloc(len);
			if (!sysctl(mib, 4, kp, &len, NULL, 0))
			{
				kp_count = len/sizeof(struct kinfo_proc);
			} else {
				kp_count = 0;
			}
		}
	}
	if (kp_count) {
		char* buffer = malloc(kp_count*5+3);
		char* bp = stpcpy(buffer, "\020$");
		for (i = kp_count-1; i >= 0; --i) {
			bp += sprintf(bp, "%i,", kp[i].kp_proc.p_pid);
		}
		bp = stpcpy(bp-1, "p");
		write(STDOUT_FILENO, buffer, bp-buffer);
	} else {
		write(STDOUT_FILENO, "\020$p", 3);
	}
	if (kp != NULL)
		free(kp);
#else
	write(STDOUT_FILENO, "\020$p", 3);
#endif
}
