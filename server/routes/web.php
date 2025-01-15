<?php

use App\Models\Pet;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    $pet = Pet::find(13);
    return view('welcome', compact('pet'));
});
