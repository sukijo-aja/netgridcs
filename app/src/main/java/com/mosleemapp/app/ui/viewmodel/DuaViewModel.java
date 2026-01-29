package com.mosleemapp.app.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mosleemapp.app.data.local.entity.DuaEntity;
import com.mosleemapp.app.data.repository.DuaRepository;

import java.util.List;

public class DuaViewModel extends AndroidViewModel {

    private DuaRepository repository;
    private LiveData<List<DuaEntity>> allDuas;

    public DuaViewModel(@NonNull Application application) {
        super(application);
        repository = new DuaRepository(application);
        allDuas = repository.getAllDuas();
    }

    public LiveData<List<DuaEntity>> getAllDuas() {
        return allDuas;
    }

    public LiveData<List<DuaEntity>> getDuasByCategory(String category) {
        return repository.getDuasByCategory(category);
    }

    public LiveData<List<String>> getAllCategories() {
        return repository.getAllCategories();
    }

    public void syncDuas() {
        repository.fetchDuasFromApi();
    }
}
