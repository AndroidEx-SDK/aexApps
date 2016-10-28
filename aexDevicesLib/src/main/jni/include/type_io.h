
#ifndef _TYPE_IO_H_
#define _TYPE_IO_H_

/* bsd */
typedef unsigned char		u_char;
typedef unsigned short		u_short;
typedef unsigned int		u_int;
typedef unsigned long		u_long;

/* sysv */
typedef unsigned char		uchar;
typedef unsigned short		ushort;
typedef unsigned int		uint;
typedef unsigned long		ulong;


typedef unsigned long long  IMG_UINT64;
 

typedef volatile unsigned long	vu_long;
typedef volatile unsigned short vu_short;
typedef volatile unsigned char	vu_char;


typedef  char s8;
typedef unsigned char u8;
typedef unsigned char U8;
typedef unsigned char uint8;
typedef signed char i8;

typedef  short s16;
typedef unsigned short u16;
typedef unsigned short U16;
typedef signed short i16;

typedef  int s32;
typedef  int S32;
typedef unsigned int u32;
typedef unsigned int U32;
typedef unsigned int uint32;
typedef signed int i32;

//typedef  int    size_t ;


//typedef char __s8;
typedef unsigned char __u8;

typedef  short __s16;
typedef unsigned short __u16;

typedef  int __s32;
typedef unsigned int __u32;



#define  NULL  0x0



#define IO_WRITEU32(reg,val) *(volatile u32*)(reg) = (val)

#define IO_WRITEU16(reg,val) *(volatile u16*)(reg) = (val)

#define IO_WRITEU8(reg,val) *(volatile u8*)(reg)   = (val)

#define IO_OR_U32(reg,val) *(volatile u32*)(reg)  |= (val)

#define IO_OR_U16(reg,val) *(volatile u16*)(reg)  |= (val)

#define IO_OR_U8(reg,val) *(volatile u8*)(reg)    |= (val)

#define IO_AND_U32(reg,val) *(volatile u32*)(reg) &= (val)

#define IO_AND_U16(reg,val) *(volatile u16*)(reg) &= (val)

#define IO_AND_U8(reg,val) *(volatile u8*)(reg)   &= (val)
#define IO_ADD(reg,val) *(volatile u32*)(reg) += (val)
#define IO_SUB(reg,val) *(volatile u32*)(reg) -= (val)


#define IO_READU32(reg) (*(volatile u32*)(reg)) 
#define IO_READU16(reg) (*(volatile u16*)(reg)) 
#define IO_READU8(reg)  (*(volatile u8*)(reg)) 

#define IO_SET_MSK_U32(reg,mask,val) *(volatile u32*)(reg) = (((*(volatile u32*)(reg))&(~(mask)))|((mask)&(val)))

#define IO_READ(reg)                 IO_READU32(reg)
#define IO_WRITE(reg,val)            IO_WRITEU32(reg,val)
#define IO_OR(reg,val)               IO_OR_U32(reg,val)
#define IO_AND(reg,val)              IO_AND_U32(reg,val)
#define IO_SET_MASK(reg,mask,val)    IO_SET_MSK_U32(reg,mask,val)


//#define Read_reg(reg)			(*(volatile unsigned long *)reg)
//#define Write_reg(reg,value)  	(*(volatile unsigned long *)(reg) = value)
#define Read_reg(reg)			IO_READ(reg)
#define Write_reg(reg,value)  	IO_WRITE(reg,value)



struct test_rslt_s
{
	U32 ulFlag;  //pass: 0x88888888; fail: 0xdead0000 + ������
	U32 ulDur;   //���Ժ�ʱ	
};


#endif
