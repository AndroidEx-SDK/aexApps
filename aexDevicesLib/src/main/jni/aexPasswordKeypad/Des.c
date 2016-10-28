/*
         DES 算法程序
*/

//#include "Des.h"
#include 	<stdio.h>
#include 	<string.h>

#define		ENCRYPT		0
#define		DESCRYPT	1
#define     PINLEN      9    

	unsigned char cdesoutput[9];
	unsigned char 	cascoutput[17];
 	
	unsigned char cdeskey[9];
	unsigned char cdesinput[9];

	unsigned char	KS[16][48];
    unsigned char	E[64];

void AscToBcd( unsigned char* charbcd, unsigned char* charasc, int len )
{
	unsigned char cbuf[300], ubuf[300];  
	int lenchar, lenover;
	int i;

	lenchar = strlen((const char*)charasc);
//	TRACE("AtoB: lenchar[%d]\n",lenchar);
	if( lenchar < len*2 )
	{
		lenover = len*2 - lenchar;
		for( i=0; i<lenover;i++) 
		{
			cbuf[i] = '0';
		}
		memcpy(&cbuf[i],charasc,lenchar);
		//memcpy(&cbuf[0],charasc,lenchar);
	}
	else    memcpy(cbuf, charasc,len*2);
	cbuf[len*2] = '\0';
	
//	TRACE("AtoB: cbuf[%s] len[%d]\n",cbuf,len);	  
	
    for(i=0;i<(len*2);i++)
    {
        if (cbuf[i]>='a')
             cbuf[i]=cbuf[i]-'a'+10;
        else if (cbuf[i]>='A')
             cbuf[i]=cbuf[i]-'A'+10;
        else if (cbuf[i]>='0')
             cbuf[i]-='0';
        else cbuf[i]-=' ';
        
    //lht if (cbuf[i]<0) cbuf[i]=0; 
//    TRACE("AtoB: cbuf[%02x]\n",cbuf[i]);
    }
    for(i=0;i<(len*2);i+=2)
    	ubuf[i/2]=cbuf[i]*16+cbuf[i+1]%16; 
    
    memcpy(charbcd, ubuf, len );	
}

void BcdToAsc( unsigned char* charasc,unsigned char *charbcd,int len )
{
	int i;
	unsigned char cbuf[300], ubuf[300];

//	if( len == 0) return;
	memcpy(ubuf, (const char*)charbcd, len);
	for(i=0;i<len;i++)
	{
		cbuf[2*i] = (ubuf[i] >> 4) ;
		cbuf[2*i+1] = (ubuf[i] & 0x0f) ;     
	} 
	for(i=0;i<len*2;i++)
	{
		if(cbuf[i] < 10 ) cbuf[i] += '0' ;
		else cbuf[i] += 'A' - 10;
	}	     
	memcpy( charasc, cbuf, len*2);
	charasc[len*2] = '\0';
}

void ShowHex(char* name, unsigned char* string, int len )
{
   int i;

//   TRACE("SHowHex len[%d] name[%s] =",len,name);
   for(i=1;i<len+1;i++) 
   {
//       if( i%10 == 0 ) TRACE("\n");
//       TRACE("%02x ",*string);
       string ++;
   }
//   TRACE("\n");
}


void key_encrypt_Asc(unsigned char* input,unsigned char* key)
{    
	
	AscToBcd(cdeskey,key,8);
	AscToBcd(cdesinput,input,8);

	key_encrypt_Bcd(cdesinput,cdeskey);
}


int key_encrypt_Bcd(unsigned char* input,unsigned char* key)
{    	
	int		i,len;
	unsigned char	clear_txt[PINLEN],cipher_txt[PINLEN];
	unsigned char	key_block[PINLEN];
	unsigned char	bits[64];

	memcpy(cdeskey, key, 8 );
	memcpy(cdesinput,input, 8 );
	cdeskey[8] ='\0';
	cdesinput[8] = '\0';
	
	for(i=0;i<8;i++)
	  key_block[i] = cdeskey[i];


	len=strlen( (const char*)cdesinput);
//	if(len%2)
//	{
//		TRACE("密码不能为奇数 ..1!!\n");
		//MessageBox("密码不能为奇数","PIN ERROR",MB_OK);
//	}
           

	for (i=0;i<8;i++)
		clear_txt[i] = cdesinput[i];

	expand(key_block,bits);
	setkey(bits);
	expand(clear_txt,bits);		
	encrypt(bits,ENCRYPT);		
	compress(bits,cipher_txt);

	//memcpy(cipher_pin ,cipher_txt,8);
	cipher_txt[8]='\0';
	
	memcpy(cdesoutput, cipher_txt, 8);
	cdesoutput[8] = '0';
	BcdToAsc(cascoutput,cdesoutput,8);
	cascoutput[16] = '\0';
	//return cipher_txt;
	return 0;
}

int setkey(unsigned char* key)
{
	// ADD for include
	
	static	unsigned char	e[] = {
	32, 1, 2, 3, 4, 5,
	 4, 5, 6, 7, 8, 9,
	 8, 9,10,11,12,13,
	12,13,14,15,16,17,
	16,17,18,19,20,21,
	20,21,22,23,24,25,
	24,25,26,27,28,29,
	28,29,30,31,32, 1,
	};  
	static	unsigned char	C[64];
	static	unsigned char	D[64];
	
	 /* Permuted-choice 1 from the key bits
	 * to yield C and D.
	 * Note that bits 8,16... are left out:
	 * They are intended for a parity check.
	 */         
 
 
	// for setkey 
	static	unsigned char	PC1_C[] = {
		57,49,41,33,25,17, 9,
		 1,58,50,42,34,26,18,
		10, 2,59,51,43,35,27,
		19,11, 3,60,52,44,36,
	};
	
	// for setkey
	static	unsigned char	PC1_D[] = {
		63,55,47,39,31,23,15,
		 7,62,54,46,38,30,22,
		14, 6,61,53,45,37,29,
		21,13, 5,28,20,12, 4,
	};
	
	/*
	 * Sequence of shifts used for the key schedule.
	*/    
	
	//For setkey
	static	unsigned char	shifts[] = {
		1,1,2,2,2,2,2,2,1,2,2,2,2,2,2,1,
	};
	
	/*
	 * Permuted-choice 2, to pick out the bits from
	 * the CD array that generate the key schedule.
	 */   
	 
	// for setkey 
	static	unsigned char	PC2_C[] = {
		14,17,11,24, 1, 5,
		 3,28,15, 6,21,10,
		23,19,12, 4,26, 8,
		16, 7,27,20,13, 2,
	};
	// for setkey 
	static	unsigned char	PC2_D[] = {
		41,52,31,37,47,55,
		30,40,51,45,33,48,
		44,49,39,56,34,53,
		46,42,50,36,29,32,
	};


	// END
	register i, j, k;
	int t;

	/*
	 * First, generate C and D by permuting
	 * the key.  The low order bit of each
	 * 8-bit unsigned char is not used, so C and D are only 28
	 * bits apiece.
	 */
	for (i=0; i<28; i++) 
	{
		C[i] = key[PC1_C[i]-1];
		D[i] = key[PC1_D[i]-1];
	}
	
	/*
	 * To generate Ki, rotate C and D according
	 * to schedule and pick up a permutation
	 * using PC2.
	 */
	for (i=0; i<16; i++) 
	{
		
		 //rotate.
		
		for (k=0; k<shifts[i]; k++) 
		{
			t = C[0];
			for (j=0; j<28-1; j++)
				C[j] = C[j+1];
			C[27] = t;
			t = D[0];
			for (j=0; j<28-1; j++)
				D[j] = D[j+1];
			D[27] = t;
		}
		/*
		 * get Ki. Note C and D are concatenated.
		 */
		for (j=0; j<24; j++)
		{
			KS[i][j] = C[PC2_C[j]-1];
			KS[i][j+24] = D[PC2_D[j]-28-1];
		}
	}

	for(i=0;i<48;i++)
		E[i] = e[i];
		
	return 0;
}

int encrypt(unsigned char* block, int edflag)
{

	// ADD include
	
	static	unsigned char	IP[] = {
		58,50,42,34,26,18,10, 2,
		60,52,44,36,28,20,12, 4,
		62,54,46,38,30,22,14, 6,
		64,56,48,40,32,24,16, 8,
		57,49,41,33,25,17, 9, 1,
		59,51,43,35,27,19,11, 3,
		61,53,45,37,29,21,13, 5,
		63,55,47,39,31,23,15, 7,
	};
	
	/*
	 * Final permutation, FP = IP^(-1)
	 */   
	 
	// For encrypt 
	static	unsigned char	FP[] = {
		40, 8,48,16,56,24,64,32,
		39, 7,47,15,55,23,63,31,
		38, 6,46,14,54,22,62,30,
		37, 5,45,13,53,21,61,29,
		36, 4,44,12,52,20,60,28,
		35, 3,43,11,51,19,59,27,
		34, 2,42,10,50,18,58,26,
		33, 1,41, 9,49,17,57,25,
	};
	
	static	unsigned char	S[8][64] = {
		14, 4,13, 1, 2,15,11, 8, 3,10, 6,12, 5, 9, 0, 7,
		 0,15, 7, 4,14, 2,13, 1,10, 6,12,11, 9, 5, 3, 8,
		 4, 1,14, 8,13, 6, 2,11,15,12, 9, 7, 3,10, 5, 0,
		15,12, 8, 2, 4, 9, 1, 7, 5,11, 3,14,10, 0, 6,13,
	
		15, 1, 8,14, 6,11, 3, 4, 9, 7, 2,13,12, 0, 5,10,
		 3,13, 4, 7,15, 2, 8,14,12, 0, 1,10, 6, 9,11, 5,
		 0,14, 7,11,10, 4,13, 1, 5, 8,12, 6, 9, 3, 2,15,
		13, 8,10, 1, 3,15, 4, 2,11, 6, 7,12, 0, 5,14, 9,
	
		10, 0, 9,14, 6, 3,15, 5, 1,13,12, 7,11, 4, 2, 8,
		13, 7, 0, 9, 3, 4, 6,10, 2, 8, 5,14,12,11,15, 1,
		13, 6, 4, 9, 8,15, 3, 0,11, 1, 2,12, 5,10,14, 7,
		 1,10,13, 0, 6, 9, 8, 7, 4,15,14, 3,11, 5, 2,12,
	
		 7,13,14, 3, 0, 6, 9,10, 1, 2, 8, 5,11,12, 4,15,
		13, 8,11, 5, 6,15, 0, 3, 4, 7, 2,12, 1,10,14, 9,
		10, 6, 9, 0,12,11, 7,13,15, 1, 3,14, 5, 2, 8, 4,
		 3,15, 0, 6,10, 1,13, 8, 9, 4, 5,11,12, 7, 2,14,
	
		 2,12, 4, 1, 7,10,11, 6, 8, 5, 3,15,13, 0,14, 9,
		14,11, 2,12, 4, 7,13, 1, 5, 0,15,10, 3, 9, 8, 6,
		 4, 2, 1,11,10,13, 7, 8,15, 9,12, 5, 6, 3, 0,14,
		11, 8,12, 7, 1,14, 2,13, 6,15, 0, 9,10, 4, 5, 3,
	
		12, 1,10,15, 9, 2, 6, 8, 0,13, 3, 4,14, 7, 5,11,
		10,15, 4, 2, 7,12, 9, 5, 6, 1,13,14, 0,11, 3, 8,
		 9,14,15, 5, 2, 8,12, 3, 7, 0, 4,10, 1,13,11, 6,
		 4, 3, 2,12, 9, 5,15,10,11,14, 1, 7, 6, 0, 8,13,
	
		 4,11, 2,14,15, 0, 8,13, 3,12, 9, 7, 5,10, 6, 1,
		13, 0,11, 7, 4, 9, 1,10,14, 3, 5,12, 2,15, 8, 6,
		 1, 4,11,13,12, 3, 7,14,10,15, 6, 8, 0, 5, 9, 2,
		 6,11,13, 8, 1, 4,10, 7, 9, 5, 0,15,14, 2, 3,12,
	
		13, 2, 8, 4, 6,15,11, 1,10, 9, 3,14, 5, 0,12, 7,
		 1,15,13, 8,10, 3, 7, 4,12, 5, 6,11, 0,14, 9, 2,
		 7,11, 4, 1, 9,12,14, 2, 0, 6,10,13,15, 3, 5, 8,
		 2, 1,14, 7, 4,10, 8,13,15,12, 9, 0, 3, 5, 6,11,
	};
	
	/*
	 * P is a permutation on the selected combination
	 * of the current L and key.
	 */
	static	unsigned char	P[] = {
		16, 7,20,21,
		29,12,28,17,
		 1,15,23,26,
		 5,18,31,10,
		 2, 8,24,14,
		32,27, 3, 9,
		19,13,30, 6,
		22,11, 4,25,
	};
	
	/*
	 * The current block, divided into 2 halves.
	 */
	static	unsigned  char	L[64], R[64];
	static	unsigned char	tempL[64];
	static	unsigned char	f[64];
	
	/*
	 * The combination of the key and the input, before selection.
	 */
	static	unsigned char	preS[64];

	/*
	 * The payoff: encrypt a block.
	 */
	
	
	// END include
	int i, ii;
	register t, j, k;

	/*
	 * First, permute the bits in the input
	 */
	for (j=0; j<64; j++)
		L[j] = block[IP[j]-1];

/*============================================== */
	for (j=0;j<32;j++)
		R[j] = L[j+32];
/*============================================== */

	/*
	 * Perform an encryption operation 16 times.
	 */
	for (ii=0; ii<16; ii++) 
	{
		/*
		 * Set direction
		 */
		if (edflag)
			i = 15-ii;
		else
			i = ii;
		/*
		 * Save the R array,
		 * which will be the new L.
		 */
		for (j=0; j<32; j++)
			tempL[j] = R[j];

		/*
		 * Expand R to 48 bits using the E selector;
		 * exclusive-or with the current key bits.
		 */
		for (j=0; j<48; j++)
			preS[j] = R[E[j]-1] ^ KS[i][j];

		/*
		 * The pre-select bits are now considered
		 * in 8 groups of 6 bits each.
		 * The 8 selection functions map these
		 * 6-bit quantities into 4-bit quantities
		 * and the results permuted
		 * to make an f(R, K).
		 * The indexing into the selection functions
		 * is peculiar; it could be simplified by
		 * rewriting the tables.
		 */
		for (j=0; j<8; j++) 
		{
			t = 6*j;
			k = S[j][(preS[t+0]<<5)+
				(preS[t+1]<<3)+
				(preS[t+2]<<2)+
				(preS[t+3]<<1)+
				(preS[t+4]<<0)+
				(preS[t+5]<<4)];
			t = 4*j;
			f[t+0] = (k>>3)&01;
			f[t+1] = (k>>2)&01;
			f[t+2] = (k>>1)&01;
			f[t+3] = (k>>0)&01;
		}
	
		/*
		 * The new R is L ^ f(R, K).
		 * The f here has to be permuted first, though.
		 */
		for (j=0; j<32; j++)
			R[j] = L[j] ^ f[P[j]-1];
		/*
		 * Finally, the new L (the original R)
		 * is copied back.
		 */
		for (j=0; j<32; j++)
			L[j] = tempL[j];

	}
	/*
	 * The output L and R are reversed.
	 */
	for (j=0; j<32; j++) 
	{
		t = L[j];
		L[j] = R[j];
		R[j] = t;
	}

	
/*============================================== */
	for (j=32;j<64;j++)
		L[j] = R[j-32];
/*============================================== */
	/*
	 * The final output
	 * gets the inverse permutation of the very original.
	 */
	for (j=0; j<64; j++)
		block[j] = L[FP[j]-1];
		
	return 0;
}

int expand(unsigned char* in, unsigned char* out)
{
	int	i,j;

	for (i=0;i<8;i++)
	{
		for (j=0;j<8;j++)
		{
			*out = (in[i] <<j) & 0x80;
			if (*out == 0x80)
				*out = 0x01;
			out++;
			
		}
	}
	return 0;
}


int compress(unsigned char* in, unsigned char* out)
{
	int	temp;
	int	i,j;
	
	for(i=0;i<8;i++)
	{
		out[i] = 0;
		temp = 1;
		for (j=7;j>=0;j--)
		{
			out[i] = out[i] + ( in[i*8+j] * temp);
			temp *= 2;
		}
	}
	return 0;
}


void key_uncrypt_Asc(unsigned char* input,unsigned char* key)
{
	AscToBcd(cdeskey,key,8);
	AscToBcd(cdesinput,input,8);
    
	key_uncrypt_Bcd(cdesinput,cdeskey);

}

int key_uncrypt_Bcd(unsigned char* input,unsigned char* key)
{
	int		i;
	unsigned char	clear_txt[PINLEN];
	unsigned char	pin_block[PINLEN];
	unsigned char	key_block[PINLEN];
	unsigned char	bits[64];
    
	memcpy(cdeskey, key, 8 );
	memcpy(cdesinput, input, 8 );
    
    
	for(i=0;i<8;i++)
	  key_block[i]=cdeskey[i];
/*******
	key_block[0] = 0x31; key_block[1] = 0x31;
	key_block[2] = 0x31; key_block[3] = 0x31;
	key_block[4] = 0x31; key_block[5] = 0x31;
	key_block[6] = 0x31; key_block[7] = 0x31;
********/
	/* get  pan_block */
	expand(key_block,bits);
	setkey(bits);
	/* set key */

/*****
	pin[0] = 0x65; pin[1] = 0x5e;
	pin[2] = 0xa6; pin[3] = 0x28;
	pin[4] = 0xcf; pin[5] = 0x62;
	pin[6] = 0x58; pin[7] = 0x5f;

*******/

	expand(cdesinput,bits);		/* expand to bit stream */
	encrypt(bits,DESCRYPT);		/* descrypt */
	compress(bits,clear_txt);	/* compress to 8 characters */

	for (i=0;i<8;i++)
		pin_block[i] = clear_txt[i];
/*****
	len=pin_block[0]&0x0f;
	for(i=0;i<len/2;i++)
	{
	     cipher_txt[2*i]= ((pin_block[i+1]>>4)&0x0f)+'0';
	     cipher_txt[2*i+1]= (pin_block[i+1]&0x0f)+'0';
	     }
*******/

	memcpy(cdesoutput, pin_block, 8);
	cdesoutput[8] = '0';
	BcdToAsc(cascoutput,cdesoutput,8);

	return 0;
}


void pan_encrypt_Bcd(unsigned char* input, unsigned char* pan,unsigned char* key)
{  

	int		i,len;
	unsigned char	clear_txt[8],cipher_txt[8];
	unsigned char	pin_block[8],pan_block[8];
	unsigned char	key_block[8];
	unsigned char	bits[64];

	memcpy(cdeskey, key, 8 );
	memcpy(cdesinput,input, 8 );
	cdeskey[8] ='\0';
	cdesinput[6] = '\0';
	pan[16] = '\0';
	
//	TRACE("input[%s] pan[%s] key[%s]\n",cdesinput,pan,cdeskey);

	for(i=0;i<8;i++)
	  key_block[i]=cdeskey[i];
/****
	key_block[0] = 0x31; key_block[1] = 0x31;
	key_block[2] = 0x31; key_block[3] = 0x31;
	key_block[4] = 0x31; key_block[5] = 0x31;
	key_block[6] = 0x31; key_block[7] = 0x31;
*****/
	pan_block[0]=pan_block[1]='\0';
	for (i=1;i<7;i++)
		pan_block[i+1] = ( (pan[2*i+1]-'0') <<4)
				 | (pan[2*i+2]-'0') ;
    len=strlen((const char*) cdesinput);
//    if(len%2)
//    {
//		TRACE("密码不能为奇数..0!!\n");
//	}
	pin_block[0]=len;
	for(i=len/2+1;i<8;i++)
		pin_block[i] = 0xff;
	for ( i=0;i<len/2;i++)
		pin_block[i+1] = ((cdesinput[2*i]-'0')<<4) 
				| (cdesinput[2*i+1]-'0');

	for (i=0;i<8;i++)
		clear_txt[i] = pin_block[i] ^ pan_block[i];

	expand(key_block,bits);
	setkey(bits);
	expand(clear_txt,bits);		/* expand to bit stream */
	encrypt(bits,ENCRYPT);		/* encrypt */
	compress(bits,cipher_txt);	/* compress to 8 characters */

	memcpy(cdesoutput, cipher_txt, 8);
	cdesoutput[8] = '0';
	BcdToAsc(cascoutput,cdesoutput,8);

}



void pan_encrypt_Asc(unsigned char* input, unsigned char* pan,unsigned char* key)
{

	AscToBcd(cdeskey,key,8);
	AscToBcd(cdesinput,input,8);
	
	pan_encrypt_Bcd(cdesinput, pan, cdeskey);
}

void pan_uncrypt_Bcd(unsigned char* input,unsigned  char* pan,unsigned char* key)
{ 
	int		i,len;
	unsigned char	clear_txt[PINLEN],cipher_txt[17];
	unsigned char	pin_block[PINLEN],pan_block[PINLEN];
	unsigned char	key_block[PINLEN];
	unsigned char	bits[64];
                       
	memcpy(cdeskey, key, 8 );
	memcpy(cdesinput, input, 8 );
                       
                       
	for(i=0;i<8;i++)
	  key_block[i]=cdeskey[i];
 /*****
	key_block[0] = 0x31; key_block[1] = 0x31;
	key_block[2] = 0x31; key_block[3] = 0x31;
	key_block[4] = 0x31; key_block[5] = 0x31;
	key_block[6] = 0x31; key_block[7] = 0x31;
 *****/
	/* get  pan_block */
	pan_block[0]=pan_block[1]=0;
	for (i=1;i<7;i++)
		pan_block[i+1] = ( (pan[2*i+1]-'0') <<4)
				 | (pan[2*i+2]-'0') ;

	expand(key_block,bits);
	setkey(bits);                   /* set key */
/********
	pin[0] = 0x65; pin[1] = 0x5e;
	pin[2] = 0xa6; pin[3] = 0x28;
	pin[4] = 0xcf; pin[5] = 0x62;
	pin[6] = 0x58; pin[7] = 0x5f;
*********/
	expand(cdesinput,bits);		/* expand to bit stream */
	encrypt(bits,DESCRYPT);		/* descrypt */
	compress(bits,clear_txt);	/* compress to 8 characters */
	for (i=0;i<8;i++)
		pin_block[i] = clear_txt[i] ^ pan_block[i];
                                        /* get pin_block   */
        len=pin_block[0]&0x0f;
        for(i=0;i<len/2;i++)
		{
             cipher_txt[2*i]= ((pin_block[i+1]>>4)&0x0f)+'0';
             cipher_txt[2*i+1]= (pin_block[i+1]&0x0f)+'0';
        }
	memcpy(cdesoutput, cipher_txt, 8);
	cdesoutput[8] = '0';
	BcdToAsc(cascoutput,cdesoutput,8);
	
}


void pan_uncrypt_Asc(unsigned char* input, unsigned char* pan,unsigned char* key)
{                                    
	 AscToBcd(cdeskey,key,8);
	 AscToBcd(cdesinput,input,8);
    
    pan_uncrypt_Bcd( cdesinput, pan, cdeskey);

}

int StringToMAC(unsigned char * string, unsigned char * MacKey,unsigned char * MAC)
{
	int zeros;
	int t;
	int i = 0;
	int j = 0;
	unsigned char dataTemp[9];
	unsigned char cToXO[9];
	memset(cToXO,0x00,9);
	if((zeros=strlen((char *)string)%8)!=0)
	{
		return 1;
	}
	t=strlen((char *)string)/8;
	for(i=0;i<t;i++)
	{
		memcpy(dataTemp,&string[i*8],8);
		for(j=0;j<8;j++)
		{
			dataTemp[j]=dataTemp[j]^cToXO[j] ;
			dataTemp[8]='\0';
		}
		key_encrypt_Bcd(dataTemp,MacKey);
		memcpy(cToXO,cdesoutput,9);
	}
	BcdToAsc(MAC,cToXO,8);
//	TRACE("MAC is %s\n",MAC);
	return 0;
}

void Des(unsigned char *pin, unsigned char *workkey, unsigned char *cipher_pin)
{
	int		i;
	unsigned char	clear_txt[PINLEN],cipher_txt[PINLEN];
//	unsigned char	pin_block[PINLEN],pan_block[PINLEN];
    unsigned char	key_block[PINLEN];
    unsigned char	bits[64];
	for(i=0;i<8;i++)
	    key_block[i]=workkey[i];

	for (i=0;i<8;i++)
		clear_txt[i] = pin[i];

	expand(key_block,bits);
	setkey(bits);
	expand(clear_txt,bits);		/* expand to bit stream */
	encrypt(bits,ENCRYPT);		/* encrypt */
	compress(bits,cipher_txt);	/* compress to 8 characters */

	memcpy(cipher_pin ,cipher_txt,8);
}

void Undes(unsigned char *pin, unsigned char *workkey, unsigned char *cipher_pin)
{
	int		i;
	unsigned char	clear_txt[PINLEN];
	unsigned char	pin_block[PINLEN];
	unsigned char	key_block[PINLEN];
	unsigned char	bits[64];

	for(i=0;i<8;i++)
	  key_block[i]=workkey[i];
	/* get  pan_block */
	expand(key_block,bits);
	setkey(bits);
	/* set key */

	expand(pin,bits);		/* expand to bit stream */
	encrypt(bits,DESCRYPT);		/* descrypt */
	compress(bits,clear_txt);	/* compress to 8 characters */

	for (i=0;i<8;i++)
		pin_block[i] = clear_txt[i];
	memcpy(cipher_pin ,pin_block, 8);
}

void TriDes(unsigned char *input, unsigned char *doublekey, unsigned char *output)
{
    unsigned char left_key[9];
    unsigned char right_key[9];
    unsigned char tmpstr1[9], tmpstr2[9];
    
    memcpy( left_key, doublekey, 8 );
    memcpy( right_key, doublekey+8, 8 );

    Des( input, left_key, tmpstr1 );
    Undes( tmpstr1, right_key, tmpstr2 );
    Des( tmpstr2, left_key, output );
}

//int main(int argc, char **argv)
//{
//	return 0;
//}
