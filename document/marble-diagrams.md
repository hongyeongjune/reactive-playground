### Timeline (left to right)
* Marble Diagram 은 기본적으로 왼쪽에서 오른쪽으로 읽는다.
![images](https://miro.medium.com/v2/resize:fit:1266/format:webp/1*brbCs4smjZfqitE0kHSHTQ.png)

### Stream completes successfully
* 원, 오각형, 삼격형 순으로 emit(통지)되었다는 것을 알 수 있다.
* 수직으로 된 바는 데이터의 emit 이 정상적으로 끝났다는 것을 알려준다.
![images](https://miro.medium.com/v2/resize:fit:1272/format:webp/1*b-7_jU--CKfTkZ3hL66U6Q.png)

### Stream terminates with an error
* 3개의 항목이 emit(통지)된 후 스트림이 오류와 함께 종료된 것을 알 수 있다.
* X 표시는 오류가 발생했다는 것 이다.
![images](https://miro.medium.com/v2/resize:fit:1278/format:webp/1*DxXNdInXrcKT0Jg3WdGafQ.png)

### Stream does not terminate
* 3개의 항목이 emit(통지)되었고, 스트림이 종료되지 않았음을 보여준다.
![images](https://miro.medium.com/v2/resize:fit:1272/format:webp/1*WrctZLIzj2Ptr5qrfHlW4g.png)

### Common Operators
1. [filter()](#filter)
2. [map()](#map)
3. [flatMap()](#flatmap)
4. [concatMap()](#concatmap)
5. [concat()](#concat)

* 더 자세한 연산자는 아래 참조 링크에서 확인할 수 있다.

### filter()
![images](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*t7F6N5eo7IQiq44VkjQMQQ.png)

### map()
![images](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*LNmVKOum63rRnln1fGM7dA.png)

### flatMap()
1. 1개의 빨간색 원이 emit 되어 2개의 빨간색 다이아몬드가 emit 된다.
2. 1개의 녹색 원이 emit 되어 2개의 녹색 다이아몬드가 emit 된다.
3. 1개의 파란색 원이 emit 되어 2개의 파란색 다이어몬드가 emit 된다.
![images](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*HnjrvlaOGvVRmSs1uSUiqA.png)

### concatMap()
* 원래 Sequence 를 유지하면서 동일한 색상의 다이아몬드를 2개씩 emit 한다.
![images](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*0O1r-YUeJ3mncOnrZayV6Q.png)

### concat()
![images](https://miro.medium.com/v2/resize:fit:1400/format:webp/1*_b_Ty47EQJQkBQxmItoAww.png)

> https://medium.com/@jshvarts/read-marble-diagrams-like-a-pro-3d72934d3ef5