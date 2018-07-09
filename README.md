# ViewInject-ButterKnife
仿照ButterKnife实现View注入

___


## 一、项目结构说明

#### 1.butterknifer-annotation：项目中用到的注解，都在此模块中定义；
#### 2.butterknifer-compiler:主工程annotationProcessor此模块，通过apt编译生成指定java文件；只在编译器有效，在运行时不会打包到apk文件里；
#### 3.butterknifer-core:用来定义butterknifer库中通用的接口，以及初始化java文件；

## 二、具体使用方式--同ButterKnifer一致（目前只实现View注入和onClickLister事件的绑定；
具体使用方式如下：

```
import com.example.BindView;
import com.example.ButterKnifer;
import com.example.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_text)
    public TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnifer.bind(this);


    }

    @OnClick(R.id.tv_text)
    public void testOnClick(View view) {
        Toast.makeText(this, "点击了View", Toast.LENGTH_SHORT).show();
    }
}
```




