      subroutine getrow(m, n, k, a, b)
      implicit none
c     get the kth row from the (n*m) matrix a
c     put into b
      double precision a(n,m), b(m)
      integer m, n, j, k
      j = 1
      do while(j .le. m)
          b(j) = a(k,j)
          j = j+1
      enddo
      end


      subroutine getmat(m, n, k, a, b)
      implicit none
c     get the kth layer from array a
c     put into b
      double precision a(n,m,m), b(m,m)
      integer i, j, k, m, n
      i = 1
      do while(i .le. m)
          j = 1
          do while(j .le. m)
              b(i,j) = a(k,i,j)
              j = j+1
          enddo
          i = i+1
      enddo
      end
