<?php

namespace Database\Seeders;

use App\Models\Pet;
use App\Models\User;
// use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        Pet::factory(2)->create();
        User::factory(3)->create();

        User::factory()->create([
            'name' => 'Admin',
            'email' => 'admin@gmail.com',
            'password' => Hash::make('password'),
            'remember_token' => Str::random(10),
            'is_admin' => true
        ]);
        User::factory()->create([
            'name' => 'Not Admin',
            'email' => 'notadmin@gmail.com',
            'password' => Hash::make('password'),
            'remember_token' => Str::random(10),
            'is_admin' => false
        ]);
    }
}
