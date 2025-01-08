<?php

namespace App\Http\Controllers;

use App\Models\Pet;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Storage;

class PetController extends Controller
{
    public function index()
    {
        $pets = Pet::latest()->get();

        if (empty($pets)) {
            return response()->json([
                'status' => false,
                'message' => 'No pets found'
            ]);
        }

        return response()->json([
            'status' => true,
            'message' => 'Pets retrieved successfully',
            'data' => $pets
        ]);
    }

    public function show($id)
    {
        $pet = Pet::find($id);

        if (!$pet) {
            return response()->json([
                'status' => false,
                'message' => 'Pet not found'
            ], 404);
        }

        return response()->json($pet);
    }


    public function store(Request $request)
    {
        try {
            $validated = $request->validate([
                'name' => 'required|string|max:255',
                'type' => 'required|string|max:255',
                'description' => 'required|string',
                'image' => 'required|image|mimes:jpeg,png,jpg|max:2048'
            ]);

            if ($request->hasFile('image')) {
                $image = $request->file('image');
                $imageName = $image->hashName();
                $image->storeAs('images', $imageName, ['disk' => 'public']);
                $validated['image'] = asset('/storage/images/' . $imageName);
            }

            $pet = Pet::create($validated);

            return response()->json([
                'status' => true,
                'message' => 'Pet created successfully',
                'data' => $pet
            ], 201);
        } catch (\Exception $e) {
            Log::error('Error creating pet: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error'
            ], 500);
        }
    }

    public function update(Request $request, $id)
    {
        try {
            $pet = Pet::find($id);

            if (!$pet) {
                return response()->json([
                    'status' => false,
                    'message' => 'Pet not found'
                ], 404);
            }

            $validated = $request->validate([
                'name' => 'required|string|max:255',
                'type' => 'required|string|max:255',
                'description' => 'required|string',
                'image' => 'nullable|image|mimes:jpeg,png,jpg|max:2048',
            ]);

            if ($request->hasFile('image')) {
                if ($pet->image) {
                    $imagePath = explode('/', $pet->image);
                    $imageFileName = end($imagePath);
                    Storage::disk('public')->delete('images/' . $imageFileName);
                }

                $image = $request->file('image');
                $imageName = $image->hashName();
                $image->storeAs('images', $imageName, ['disk' => 'public']);
                $validated['image'] = asset('storage/images/' . $imageName);
            }

            $pet->update($validated);
            return response()->json([
                'status' => true,
                'message' => 'Pet updated successfully',
                'data' => $pet
            ]);
        } catch (\Exception $e) {
            Log::error('Error updating pet: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error'
            ], 500);
        }
    }


    public function destroy(Pet $pet)
    {
        try {
            $imagePath = explode('/', $pet->image);
            $imageFileName = end($imagePath);

            if ($imageFileName) {
                $isImageDeleted = Storage::disk('public')->delete('images/' . $imageFileName);
            } else {
                return response()->json([
                    'status' => false,
                    'message' => 'Image not found'
                ], 404);
            }

            if ($isImageDeleted) {
                $pet->delete();
                return response()->json([
                    'status' => true,
                    'message' => 'Pet deleted successfully',
                    'data' => $pet
                ]);
            }

            return response()->json([
                'status' => false,
                'message' => 'Failed to delete image'
            ], 500);
        } catch (\Exception $e) {
            Log::error('Error deleting pet: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'message' => 'Internal Server Error'
            ], 500);
        }
    }
}
