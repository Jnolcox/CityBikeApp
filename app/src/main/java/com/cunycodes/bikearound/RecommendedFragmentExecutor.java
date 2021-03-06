package com.cunycodes.bikearound;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;


public class RecommendedFragmentExecutor extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private UserDBHelper helper;
    private SQLiteDatabase database;
    private String userMembership;
    private TextView nav_name;                                                                        // added by Jody --do not delete, comment out if you need to operate without user
    private TextView nav_membership;
    private Context context;
    private Uri uri;
    private String stringUri;
    private ImageView mUsers_photo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rec_fragments);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        context = this.getBaseContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(RecommendedFragmentExecutor.this);
        View header = navigationView.getHeaderView(0);
        mUsers_photo = (ImageView) header.findViewById(R.id.users_photo);
        nav_name = (TextView) header.findViewById(R.id.user_name);
        nav_membership = (TextView) header.findViewById(R.id.user_membership);
        nav_name.setText(user.getDisplayName());
        setUP();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), RecommendedFragmentExecutor.this);
        viewPager.setAdapter(pageAdapter);

        final TabLayout layout = (TabLayout) findViewById(R.id.tab_layout);
        layout.setupWithViewPager(viewPager);

        for (int i =0; i<layout.getTabCount(); i++){
            TabLayout.Tab tab = layout.getTabAt(i);
            tab.setCustomView(pageAdapter.getTabview(i));
        }

    }

    public void setUP(){
        String userName = user.getDisplayName();
        helper = new UserDBHelper(getApplicationContext());
        database = helper.getReadableDatabase();
        Cursor cursor = helper.getMembership(userName, database);
        if (cursor.moveToFirst()){
            userMembership = cursor.getString(0);
            nav_membership.setText(userMembership);
        }

        Cursor cursor1 = helper.getPhotoURI(userName, database);
        if (cursor1.moveToFirst()){
            stringUri = cursor1.getString(0);
            uri = Uri.parse(stringUri);
            if (uri!= null){
                displayPhoto();
            } else {
                mUsers_photo.setImageResource(R.mipmap.placeholder_woman);
            }
        }
        helper.close();
    }

    public void displayPhoto(){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Matrix matrix = new Matrix();
            int rotate = getOrientation(uri);
            if (rotate == 0 && (bitmap.getWidth()> bitmap.getHeight())){
                matrix.postRotate(90);
            } else {
                matrix.postRotate(rotate);
            }
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0,0, bitmap.getWidth(),bitmap.getHeight(),matrix, true );
            mUsers_photo.setImageBitmap(newBitmap);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Failed To Load Photo", Toast.LENGTH_SHORT).show();
        }

    }

    public int getOrientation(Uri uri){
        ExifInterface exifInterface = null;
        int rotate = 0;
        try {
            exifInterface = new ExifInterface(uri.getPath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                rotate = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                rotate = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                rotate = 270;
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Orientation not defined", Toast.LENGTH_SHORT).show();
        }

        return rotate;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_recommend){
            Intent intent = new Intent(this, FoursquarePath.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.nav_settings){
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_plan){
            Intent intent = new Intent(this, PlanActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    }

    class PageAdapter extends FragmentPagerAdapter {

        String[] tabs = {"New York", "Brooklyn", "Queens", "Bronx"};
        Context context;

        public PageAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new NewYorkFragment();
                case 1:
                    return new BrooklynFragment();
                case 2:
                    return new QueensFragment();
                case 3:
                    return new BronxFragment();
              /*  case 3:
                    return new NewYorkPathFragment();
                case 4:
                    return new NewYorkPathFragment(); */
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        public View getTabview(int i){
            View tab = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
            TextView tv = (TextView) tab.findViewById(R.id.custom_text);
            tv.setText(tabs[i]);
            return tab;
        }

    }
